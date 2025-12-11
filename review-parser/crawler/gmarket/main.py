import time

from common.platform import Platform
from crawler.gmarket.gmarket_review_parser import GmarketReviewParser
from common.kafka_utils import create_consumer
from common.kafka_utils import create_producer
from common.logging_utils import setup_logger
import logging

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
    parser = GmarketReviewParser()

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[gmarket-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 리뷰 정보 파싱 실행
        for url in urls:
            # 파싱 시작
            parser_start = time.perf_counter()

            product_reviews = {}
            reviews = parser.get_reviews(url)
            if (len(reviews) < 1):
                logging.info(f"[no-review] 리뷰가 존재하지 않아 메시지를 발행하지 않습니다.")
                # 파싱 완료
                parser_end = time.perf_counter()
                logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")
                continue

            product_reviews["main_product_id"] = main_product_id
            product_reviews["platform"] = Platform.GMARKET.value
            product_reviews["reviews"] = reviews

            producer.send('product-reviews', product_reviews)
            # 파싱 종료
            parser_end = time.perf_counter()
            logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")
            logging.info(f"[publish] product-reviews: {product_reviews}")

    parser.quit()
total_end = time.perf_counter()
logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")

