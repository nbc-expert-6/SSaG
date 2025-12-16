import time

from common.kafka_utils import create_consumer, create_producer
from common.monitoring.logger import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from common.platform import Platform
from crawler.elevenst.elevenst_review_parser import ElevenStReviewParser

# [PERF] 전체 코드 실행 start
total_start = time.perf_counter()

# Kafka Consumer 설정
consumer = create_consumer(
    topic='elevenst-product-urls',
    group_id='elevenst-review-group'
)

# Kafka Producer 설정
producer = create_producer()

# Logging 설정
logger = setup_logger("elevenst")

if __name__ == "__main__":
    start_metrics_server()

    parser = ElevenStReviewParser()

    for url_info in consumer:
        success = True

        # 받은 url 정보
        logger.info("KAFKA_CONSUME", value=f"{url_info.value}")
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 리뷰 정보 파싱 실행
        for url in urls:
            # [PERF] review 파싱 start
            parser_start = time.perf_counter()
            CRAWL_TRIAL_COUNT.labels(platform="elevenst").inc()

            try:
                product_reviews = {}
                reviews = parser.get_reviews(url)

                if len(reviews) < 1:
                    logger.info("REVIEW_NO_RESULT")

                    parser_end = time.perf_counter()
                    CRAWL_LATENCY.labels(platform="elevenst").observe(parser_end - parser_start)
                    logger.perf("PARSING_COMPLETED", time=round(parser_end - parser_start, 4))

                    continue

                product_reviews["main_product_id"] = main_product_id
                product_reviews["platform"] = Platform.ELEVENST.value
                product_reviews["reviews"] = reviews

                # 파싱 종료
                parser_end = time.perf_counter()
                CRAWL_LATENCY.labels(platform="elevenst").observe(parser_end - parser_start)
                logger.perf("PARSING_COMPLETED", time=round(parser_end - parser_start, 4))

                kafka_start = time.perf_counter()
                producer.send('product-reviews', product_reviews)
                kafka_end = time.perf_counter()
                KAFKA_PUBLISH_LATENCY.observe(kafka_end - kafka_start)
                KAFKA_PUBLISH_COUNT.inc()

                logger.info("KAFKA_PUBLISH", product_reviews=product_reviews)

            except Exception as e:
                success = False
                logger.error("PARSING_FAILED",
                             main_product_id=main_product_id,
                             url=url,
                             exception=str(e),
                             exc_info=True)
                CRAWL_EXCEPTION_COUNT.labels(platform="elevenst").inc()

        if success:
            consumer.commit()

    parser.quit()

    # [PERF] 전체 코드 실행 end
    total_end = time.perf_counter()
    logger.info("PROGRAM_EXITED", total_time=round(total_end - total_start, 4))
