from crawler.gmarket.gmarket_detail_parser import GmarketDetailParser
from ...common.kafka_utils import create_consumer
from ...common.kafka_utils import create_producer
from ...common.logging_utils import setup_logger
import logging

# Kafka Consumer 설정
consumer = create_consumer(
    topic='gmarket_links',
    group_id='gmarket-detail-group'
)

# Kafka Producer 설정
producer = create_producer()

# Logging 설정
setup_logger()

if __name__ == "__main__":
    parser = GmarketDetailParser()

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[gmarket-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 상세 정보 파싱 실행
        for url in urls:
            product_details = parser.get_product_details(url)
            product_details["main_product_id"] = main_product_id
            product_details["platform"] = "auction"
            product_details["sale_link"] = url

            producer.send('product-details', product_details)
            logging.info(f"[publish] product-details: {product_details}")

    # # 테스트용
    # urls = ["https://itempage3.auction.co.kr/DetailView.aspx?itemno=F301578522",
    #         "https://itempage3.auction.co.kr/DetailView.aspx?itemno=F366343357"]
    # for url in urls:
    #     product_details = parser.get_product_details(url)
    #     product_details["main_product_id"] = "main_product_id"
    #     product_details["platform"] = "auction"
    #     product_details["sale_link"] = url
    #
    #     logging.info(f"[publish] product-details: {product_details}")

    parser.quit()
