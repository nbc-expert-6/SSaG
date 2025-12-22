import time
import traceback

from common.exceptions import DetailParseException
from common.kafka_utils import create_consumer, create_producer
from common.monitoring.logger import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from common.platform import Platform
from crawler.elevenst.elevenst_detail_parser import ElevenStDetailParser

DLQ_TOPIC = "elevenst-detail.dlq"
PRODUCT_DETAILS_TOPIC = "product-details"
PLATFORM = Platform.ELEVENST.value

# 시작 시간
total_start = time.perf_counter()

# Kafka Consumer 설정
consumer = create_consumer(
    topic='elevenst-product-urls',
    group_id='elevenst-detail-group'
)

# Kafka Producer 설정
producer = create_producer()

# Logging 설정
logger = setup_logger(PLATFORM)

if __name__ == "__main__":
    start_metrics_server()
    parser = ElevenStDetailParser()

    for url_info in consumer:
        # 받은 url 정보
        value = url_info.value
        logger.info("KAFKA_CONSUME", value=f"{value}")
        main_product_id = value["main_product_id"]
        urls = value["urls"]

        # 제품 상세 정보 파싱 실행
        for url in urls:
            # 파싱 시작
            parser_start = time.perf_counter()
            CRAWL_TRIAL_COUNT.labels(platform=PLATFORM).inc()

            try:
                product_details = parser.get_product_details(url)
                product_details["main_product_id"] = main_product_id
                product_details["platform"] = PLATFORM

                # 파싱 종료
                parser_end = time.perf_counter()
                CRAWL_LATENCY.labels(platform=PLATFORM).observe(parser_end - parser_start)
                logger.perf(
                    "PARSING_COMPLETED",
                    time=round(parser_end - parser_start, 4)
                )

                kafka_start = time.perf_counter()
                producer.send(PRODUCT_DETAILS_TOPIC, product_details)
                kafka_end = time.perf_counter()
                KAFKA_PUBLISH_LATENCY.observe(kafka_end - kafka_start)
                KAFKA_PUBLISH_COUNT.inc()

                logger.info("KAFKA_PUBLISH", product_details=product_details)

                # logger.info(
                #     "DETAIL_PARSE_SUCCESS",
                #     main_product_id=main_product_id,
                #     url=url,
                #     elapsed=round(parser_end - parser_start, 4),
                # )
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
                continue
            except Exception as e:
                # DetailParseException 아닌 예외
                logger.exception(
                    "UNEXPECTED_EXCEPTION",
                    main_product_id=main_product_id,
                    url=url,
                    original_exception_type=type(e).__name__,
                    exc_info=True,
                )
                CRAWL_EXCEPTION_COUNT.labels(platform=PLATFORM).inc()
                continue

        producer.flush()
        consumer.commit()

    parser.quit()
    total_end = time.perf_counter()
    logger.info("PROGRAM_EXITED", total_time=round(total_end - total_start, 4))
