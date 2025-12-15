import logging
import time

from common.kafka_utils import create_consumer, create_producer
from common.logging_utils import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from common.platform import Platform
from crawler.gmarket.gmarket_review_parser import GmarketReviewParser

# 해당 크기만큼 처리 후 브라우저 리셋
MAX_BATCH = 20

# 시작 시간
total_start = time.perf_counter()

# Kafka Consumer 설정
consumer = create_consumer(
    topic='gmarket-product-urls',
    group_id='gmarket-review-group'
)

# Kafka Producer 설정
producer = create_producer()

# Logging 설정
setup_logger()

if __name__ == "__main__":
    start_metrics_server()

    parser = GmarketReviewParser()

    # 처리한 url 숫자
    # MAX_BATCH로 나누어 떨어질 때 마다 브라우저 리셋
    processed_count = 0

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[gmarket-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 리뷰 정보 파싱 실행
        for url in urls:
            # ===== 브라우저 리셋 체크 =====
            if processed_count > 0 and processed_count % MAX_BATCH == 0:
                logging.info(f"브라우저 리셋: {processed_count}건 처리됨, 새 Chrome 시작")
                parser.quit()
                parser = GmarketReviewParser()
                time.sleep(1)
            # 파싱 시작
            parser_start = time.perf_counter()
            CRAWL_TRIAL_COUNT.labels(platform="gmarket").inc()

            try:
                product_reviews = {}
                reviews = parser.get_reviews(url)

                if len(reviews) < 1:
                    logging.info(f"[no-review] 리뷰가 존재하지 않아 메시지를 발행하지 않습니다.")
                    # 파싱 완료
                    parser_end = time.perf_counter()
                    CRAWL_LATENCY.labels(platform="gmarket").observe(parser_end - parser_start)
                    logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")
                    processed_count += 1

                    continue

                product_reviews["main_product_id"] = main_product_id
                product_reviews["platform"] = Platform.GMARKET.value
                product_reviews["reviews"] = reviews

                # 파싱 종료
                parser_end = time.perf_counter()
                CRAWL_LATENCY.labels(platform="gmarket").observe(parser_end - parser_start)
                logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

                kafka_start = time.perf_counter()
                producer.send('product-reviews', product_reviews)
                kafka_end = time.perf_counter()
                KAFKA_PUBLISH_LATENCY.observe(kafka_end - kafka_start)
                KAFKA_PUBLISH_COUNT.inc()

                logging.info(f"[publish] product-reviews: {product_reviews}")
                processed_count += 1

            except Exception as e:
                logging.error(
                    f"처리 실패. main_product_id={main_product_id}, url={url}, error={e}",
                    exc_info=True
                )
                CRAWL_EXCEPTION_COUNT.labels(platform="gmarket").inc()

    parser.quit()

    total_end = time.perf_counter()
    logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")
