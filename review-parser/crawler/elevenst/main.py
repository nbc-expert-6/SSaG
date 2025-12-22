import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

from common.exceptions import ReviewParseException
from common.kafka_utils import create_consumer, create_producer
from common.monitoring.logger import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from common.platform import Platform
from crawler.elevenst.elevenst_review_parser import ElevenStReviewParser

DLQ_TOPIC = "elevenst-review.dlq"
PRODUCT_REVIEWS_TOPIC = "product-reviews"
PLATFORM = Platform.ELEVENST.value
MAX_WORKERS = 3

logger = setup_logger(PLATFORM)

# thread-local storage (스레드별 parser)
_thread_local = threading.local()


def get_parser():
    if not hasattr(_thread_local, "parser"):
        _thread_local.parser = ElevenStReviewParser()
        logger.info("REVIEW_PARSER_CREATED")
    return _thread_local.parser


def worker_task(url: str, main_product_id: str, producer):
    parser_start = time.perf_counter()
    CRAWL_TRIAL_COUNT.labels(platform=PLATFORM).inc()

    parser = get_parser()

    try:
        reviews = parser.get_reviews(url)

        if len(reviews) < 1:
            logger.info("REVIEW_NO_RESULT")

            parser_end = time.perf_counter()
            CRAWL_LATENCY.labels(platform=PLATFORM).observe(parser_end - parser_start)
            logger.perf(
                "PARSING_COMPLETED",
                time=round(parser_end - parser_start, 4),
            )
            return

        product_reviews = {
            "main_product_id": main_product_id,
            "platform": PLATFORM,
            "reviews": reviews
        }

        parser_end = time.perf_counter()
        CRAWL_LATENCY.labels(platform=PLATFORM).observe(parser_end - parser_start)
        logger.perf(
            "PARSING_COMPLETED",
            time=round(parser_end - parser_start, 4),
        )

        kafka_start = time.perf_counter()
        producer.send(PRODUCT_REVIEWS_TOPIC, product_reviews)
        kafka_end = time.perf_counter()

        KAFKA_PUBLISH_LATENCY.observe(kafka_end - kafka_start)
        KAFKA_PUBLISH_COUNT.inc()

        logger.info(
            "KAFKA_PUBLISH",
            product_reviews=product_reviews,
        )

    except ReviewParseException as e:
        parser_end = time.perf_counter()
        logger.error(
            "REVIEW_PARSE_FAILED",
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


if __name__ == "__main__":
    start_metrics_server()
    total_start = time.perf_counter()

    consumer = create_consumer(
        topic="elevenst-product-urls",
        group_id="elevenst-review-group",
    )
    producer = create_producer()

    executor = ThreadPoolExecutor(max_workers=MAX_WORKERS)

    try:
        for url_info in consumer:
            logger.info("KAFKA_CONSUME", value=f"{url_info.value}")

            main_product_id = url_info.value["main_product_id"]
            urls = url_info.value["urls"]


            futures = [
                executor.submit(
                    worker_task,
                    url,
                    "main_product_id",
                    producer,
                )
                for url in urls
            ]

            for future in as_completed(futures):
                # worker 내부에서 예외 처리
                future.result()

            producer.flush()
            consumer.commit()

    finally:
        executor.shutdown(wait=True)
        producer.close()

        total_end = time.perf_counter()
        logger.info(
            "PROGRAM_EXITED",
            total_time=round(total_end - total_start, 4),
        )
