from .auction_detail_parser import AuctionDetailParser
from ...common.kafka_utils import create_consumer
from ...common.logging_utils import setup_logger
import logging

# Kafka Consumer 설정
consumer = create_consumer(
    topic='auction_links',
    group_id='auction-detail-group'
)

# Logging 설정
setup_logger()

if __name__ == "__main__":
    parser = AuctionDetailParser()

    for url_info in consumer:
        # url 정보
        logging.info("url_info: {}".format(url_info.value))
        product_id = url_info.value['id']
        urls = url_info.value['urls']

        # 대표 상품에 대한 상세 상품 정보 수집
        logging.info(f"product_id: {product_id}")
        for url in urls:
            info = parser.get_product_details(url)
            logging.info(f"info: {info}")

        # TODO: kafka publish( -> product-service에서 consume)
    parser.quit()
