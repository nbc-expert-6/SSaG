import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from queue import Queue

from common.exceptions import DetailParseException
from common.kafka_utils import create_consumer, create_producer
from common.monitoring.logger import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from common.platform import Platform
from crawler.elevenst.elevenst_detail_parser import ElevenstDetailParser

DLQ_TOPIC = "elevenst-detail.dlq"
PRODUCT_DETAILS_TOPIC = "product-details"
PLATFORM = Platform.ELEVENST.value
MAX_WORKERS = 3          # Thread 수
DRIVER_POOL_SIZE = 3     # ChromeDriver 수


driver_pool: Queue[ElevenstDetailParser] = Queue(maxsize=DRIVER_POOL_SIZE)

logger = setup_logger(PLATFORM)


# Driver Pool
def init_driver_pool():
    logger.info(
        "INIT_DRIVER_POOL",
        pool_size=DRIVER_POOL_SIZE,
    )
    for _ in range(DRIVER_POOL_SIZE):
        driver_pool.put(ElevenstDetailParser())


def shutdown_driver_pool():
    logger.info("SHUTDOWN_DRIVER_POOL")
    while not driver_pool.empty():
        parser = driver_pool.get_nowait()
        try:
            parser.quit()
        except Exception:
            pass


def worker_task(url: str, main_product_id: str, producer):
    parser_start = time.perf_counter()
    CRAWL_TRIAL_COUNT.labels(platform=PLATFORM).inc()

    parser = None

    try:
        # ChromeDriver 획득 (없으면 block)
        parser = driver_pool.get()

        product_details = parser.get_product_details(url)
        product_details["main_product_id"] = main_product_id
        product_details["platform"] = PLATFORM

        parser_end = time.perf_counter()
        CRAWL_LATENCY.labels(platform=PLATFORM).observe(parser_end - parser_start)
        logger.perf(
            "PARSING_COMPLETED",
            time=round(parser_end - parser_start, 4),
        )

        kafka_start = time.perf_counter()
        producer.send(PRODUCT_DETAILS_TOPIC, product_details)
        kafka_end = time.perf_counter()

        KAFKA_PUBLISH_LATENCY.observe(kafka_end - kafka_start)
        KAFKA_PUBLISH_COUNT.inc()

        logger.info(
            "KAFKA_PUBLISH",
            product_details=product_details,
        )

    except DetailParseException as e:
        parser_end = time.perf_counter()

        logger.error(
            "DETAIL_PARSE_FAILED",
            main_product_id=main_product_id,
            url=url,
            stage=e.stage,
            reason=e.reason,
            original_exception_type=e.original_exception_type,
            elapsed=round(parser_end - parser_start, 4),
            exc_info=True,
        )

        CRAWL_EXCEPTION_COUNT.labels(platform=PLATFORM).inc()

        producer.send(
            DLQ_TOPIC,
            {
                "platform": PLATFORM,
                "main_product_id": main_product_id,
                "url": url,
                "stage": e.stage,
                "reason": e.reason,
                "original_exception_type": e.original_exception_type,
                "exception_message": str(e),
            },
        )

    except Exception as e:
        logger.exception(
            "UNEXPECTED_EXCEPTION",
            main_product_id=main_product_id,
            url=url,
            original_exception_type=type(e).__name__,
            exc_info=True,
        )
        CRAWL_EXCEPTION_COUNT.labels(platform=PLATFORM).inc()

    finally:
        # Driver 반납
        if parser is not None:
            driver_pool.put(parser)


if __name__ == "__main__":
    start_metrics_server()
    init_driver_pool()

    total_start = time.perf_counter()

    consumer = create_consumer(
        topic="elevenst-product-urls",
        group_id="elevenst-detail-group",
    )
    producer = create_producer()

    executor = ThreadPoolExecutor(max_workers=MAX_WORKERS)

    try:
        for url_info in consumer:
            value = url_info.value
            logger.info("KAFKA_CONSUME", value=value)

            main_product_id = value["main_product_id"]
            urls = value["urls"]

            futures = [
                executor.submit(
                    worker_task,
                    url,
                    main_product_id,
                    producer,
                )
                for url in urls
            ]

            for future in as_completed(futures):
                # worker 내부에서 예외 처리 및 DLQ 처리
                future.result()

            producer.flush()
            consumer.commit()

    finally:
        executor.shutdown(wait=True)
        shutdown_driver_pool()
        producer.close()

        total_end = time.perf_counter()
        logger.info(
            "PROGRAM_EXITED",
            total_time=round(total_end - total_start, 4)
        )
