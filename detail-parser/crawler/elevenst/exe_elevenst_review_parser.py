from .elevenst_review_parser import ElevenStReviewParser
from ...common.logging_utils import setup_logger
from ...common.kafka_utils import create_consumer
import logging

# Kafka Consumer 설정
consumer = create_consumer(
    topic='elevenst-product-urls',
    group_id='elevenst-detail-parser'
)

# Logging 설정
setup_logger()

if __name__ == "__main__":
    parser = ElevenStReviewParser()

    for url_info in consumer:
        logging.info(f"url_info: {url_info.value}")
        product_id = url_info.value['id']
        urls = url_info.value['urls']

        logging.info(f"product_id: {product_id}")
        for url in urls:
            reviews = parser.get_reviews(url)
            for r in reviews:
                logging.info(f"review: {r}")

    # 테스트용
    # urls = ["https://www.11st.co.kr/products/3284269705", "https://www.11st.co.kr/products/8842916818"]
    # for url in urls:
    #     reviews = parser.get_reviews(url)
    #     for r in reviews:
    #         logging.info(f"review: {r}")

    parser.quit()
