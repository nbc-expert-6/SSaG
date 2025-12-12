import time

from common.platform import Platform
from crawler.coupang.coupang_detail_parser import CoupangDetailParser
from common.kafka_utils import create_consumer
from common.kafka_utils import create_producer
from common.logging_utils import setup_logger
import logging

# 시작 시간
total_start = time.perf_counter()

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
            # 파싱 시작
            parser_start = time.perf_counter()

            product_details = parser.get_product_details(url)
            product_details["main_product_id"] = main_product_id
            product_details["platform"] = Platform.COUPANG.value
            producer.send('product-details', product_details)
            # 파싱 종료
            parser_end = time.perf_counter()
            logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")
            logging.info(f"[publish] product-details: {product_details}")

    parser.quit()
total_end = time.perf_counter()
logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")