import logging
import time

from common.kafka_utils import create_consumer, create_producer
from common.logging_utils import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from common.platform import Platform
from crawler.auction.auction_review_parser import AuctionReviewParser

# [PERF] 전체 코드 실행 start
total_start = time.perf_counter()

# Kafka Consumer 설정
consumer = create_consumer(
    topic='auction-product-urls',
    group_id='auction-review-group'
)

# Kafka Producer 설정
producer = create_producer()

# Logging 설정
setup_logger()

if __name__ == "__main__":
    start_metrics_server()
    # 임시로 6개 페이지 파싱 설정
    parser = AuctionReviewParser(max_pages=6)

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[auction-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 리뷰 정보 파싱 실행
        for url in urls:
            # [PERF] review 파싱 start
            parser_start = time.perf_counter()
            CRAWL_TRIAL_COUNT.labels(platform="auction").inc()

            try:
                product_reviews = {}
                reviews = parser.get_reviews(url)

                if len(reviews) < 1:
                    logging.info(f"[no-review] 리뷰가 존재하지 않아 메시지를 발행하지 않습니다.")

                    parser_end = time.perf_counter()
                    CRAWL_LATENCY.labels(platform="auction").observe(parser_end - parser_start)
                    logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

                    continue

                product_reviews["main_product_id"] = main_product_id
                product_reviews["platform"] = Platform.AUCTION.value
                product_reviews["reviews"] = reviews

                # 파싱 종료
                parser_end = time.perf_counter()
                CRAWL_LATENCY.labels(platform="auction").observe(parser_end - parser_start)
                logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

                kafka_start = time.perf_counter()
                producer.send('product-reviews', product_reviews)
                kafka_end = time.perf_counter()
                KAFKA_PUBLISH_LATENCY.observe(kafka_end - kafka_start)
                KAFKA_PUBLISH_COUNT.inc()

                logging.info(f"[publish] product-reviews: {product_reviews}")

            except Exception as e:
                logging.error(
                    f"처리 실패. main_product_id={main_product_id}, url={url}, error={e}",
                    exc_info=True
                )
                CRAWL_EXCEPTION_COUNT.labels(platform="auction").inc()

    parser.quit()

    # [PERF] 전체 코드 실행 end
    total_end = time.perf_counter()
    logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")
