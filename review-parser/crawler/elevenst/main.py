import logging
import time

from common.kafka_utils import create_consumer, create_producer
from common.logging_utils import setup_logger
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
setup_logger()

if __name__ == "__main__":
    parser = ElevenStReviewParser()

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[elevenst-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 리뷰 정보 파싱 실행
        for url in urls:
            # [PERF] review 파싱 start
            parser_start = time.perf_counter()

            product_reviews = {}
            reviews = parser.get_reviews(url)

            if len(reviews) < 1:
                logging.info(f"[no-review] 리뷰가 존재하지 않아 메시지를 발행하지 않습니다.")
                # [PERF] review 파싱 end
                parser_end = time.perf_counter()
                logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

                continue

            product_reviews["main_product_id"] = main_product_id
            product_reviews["platform"] = Platform.ELEVENST.value
            product_reviews["reviews"] = reviews

            producer.send('product-reviews', product_reviews)

            # [PERF] review 파싱 end
            parser_end = time.perf_counter()
            logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

            logging.info(f"[publish] product-reviews: {product_reviews}")

    parser.quit()

# [PERF] 전체 코드 실행 end
total_end = time.perf_counter()
logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")
