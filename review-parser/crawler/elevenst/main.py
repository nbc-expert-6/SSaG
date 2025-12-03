from crawler.elevenst.elevenst_review_parser import ElevenStReviewParser
from common.kafka_utils import create_consumer
from common.kafka_utils import create_producer
from common.logging_utils import setup_logger
import logging

# Kafka Consumer 설정
consumer = create_consumer(
    topic='elevenst-product-urls',
    group_id='elevenst-detail-parser'
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
            product_reviews = {}
            reviews = parser.get_reviews(url)
            product_reviews["main_product_id"] = main_product_id
            product_reviews["platform"] = "11st"
            product_reviews["reviews"] = reviews

            producer.send('product-reviews', product_reviews)
            logging.info(f"[publish] product-reviews: {product_reviews}")

    # 테스트용
    # urls = ["https://www.11st.co.kr/products/3284269705", "https://www.11st.co.kr/products/8825279288?&trTypeCd=05&trCtgrNo=585021&checkCtlgPrd=true"]
    # for url in urls:
    #     product_reviews = {}
    #     reviews = parser.get_reviews(url)
    #     product_reviews["main_product_id"] = "main_product_id"
    #     product_reviews["platform"] = "11st"
    #     product_reviews["reviews"] = reviews
    #
    #     logging.info(f"[publish] product-reviews: {product_reviews}")

    parser.quit()
