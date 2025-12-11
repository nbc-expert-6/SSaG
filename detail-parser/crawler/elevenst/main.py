import logging
import time

from common.kafka_utils import create_consumer, create_producer
from common.logging_utils import setup_logger
from common.platform import Platform
from crawler.elevenst.elevenst_detail_parser import ElevenStDetailParser

# [PERF] 전체 코드 실행 start
total_start = time.perf_counter()

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
            # [PERF] detail 파싱 start
            parser_start = time.perf_counter()

            product_details = parser.get_product_details(url)
            product_details["main_product_id"] = main_product_id
            product_details["platform"] = Platform.ELEVENST.value

            producer.send('product-details', product_details)

            # [PERF] detail 파싱 end
            parser_end = time.perf_counter()
            logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

            logging.info(f"[publish] product-details: {product_details}")

    parser.quit()

# [PERF] 전체 코드 실행 end
total_end = time.perf_counter()
logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")