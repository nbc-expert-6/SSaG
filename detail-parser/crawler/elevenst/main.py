import logging

from common.kafka_utils import create_consumer, create_producer
from common.logging_utils import setup_logger
from crawler.elevenst.elevenst_detail_parser import ElevenStDetailParser

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
    parser = ElevenStDetailParser()

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[elevenst-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 상세 정보 파싱 실행
        for url in urls:
            product_details = parser.get_product_details(url)
            product_details["main_product_id"] = main_product_id
            product_details["sale_link"] = url

            producer.send('product-details', product_details)
            logging.info(f"[publish] product-details: {product_details}")

    # # 테스트용
    # urls = ["https://www.11st.co.kr/products/8839324837?&trTypeCd=MAS101&trCtgrNo=585021&checkCtlgPrd=true",
    #         "https://www.11st.co.kr/products/8192631213?&trTypeCd=MAS101&trCtgrNo=585021&checkCtlgPrd=true"]
    # for url in urls:
    #     product_details = parser.get_product_details(url)
    #     product_details["main_product_id"] = "main_product_id"
    #     product_details["platform"] = "11st"
    #     product_details["sale_link"] = url
    #
    #     logging.info(f"[publish] product-details: {product_details}")

    parser.quit()
