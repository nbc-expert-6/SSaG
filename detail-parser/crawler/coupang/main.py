from crawler.coupang.coupang_detail_parser import CoupangDetailParser
from common.kafka_utils import create_consumer
from common.kafka_utils import create_producer
from common.logging_utils import setup_logger
import logging

# Kafka Consumer 설정
consumer = create_consumer(
    topic='coupang-product-urls',
    group_id='coupang-detail-group'
)

# Kafka Producer 설정
producer = create_producer()

# Logging 설정
setup_logger()

if __name__ == "__main__":
    parser = CoupangDetailParser()

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[coupang-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 상세 정보 파싱 실행
        for url in urls:
            product_details = parser.get_product_details(url)
            product_details["main_product_id"] = main_product_id

            producer.send('product-details', product_details)
            logging.info(f"[publish] product-details: {product_details}")

    parser.quit()
