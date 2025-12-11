import time

from common.platform import Platform
from crawler.gmarket.gmarket_detail_parser import GmarketDetailParser
from common.kafka_utils import create_consumer
from common.kafka_utils import create_producer
from common.logging_utils import setup_logger
import logging

# 해당 크기만큼 처리 후 브라우저 리셋
MAX_BATCH = 20

# 시작 시간
total_start = time.perf_counter()

# Kafka Consumer 설정
consumer = create_consumer(
    topic='gmarket-product-urls',
    group_id='gmarket-detail-group'
)

# Kafka Producer 설정
producer = create_producer()

# Logging 설정
setup_logger()

if __name__ == "__main__":
    parser = GmarketDetailParser()
    # 처리한 url 숫자
    # MAX_BATCH로 나누어 떨어질 때 마다 브라우저 리셋
    processed_count = 0
    for url_info in consumer:
        # 받은 url 정보
        logging.info("[gmarket-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 상세 정보 파싱 실행
        for url in urls:
            # ===== 브라우저 리셋 체크 =====
            if processed_count > 0 and processed_count % MAX_BATCH == 0:
                logging.info(f"브라우저 리셋: {processed_count}건 처리됨, 새 Chrome 시작")
                parser.quit()
                parser = GmarketDetailParser()
                time.sleep(1)

            # 파싱 시작
            parser_start = time.perf_counter()

            product_details = parser.get_product_details(url)
            product_details["main_product_id"] = main_product_id
            product_details["platform"] = Platform.GMARKET.value

            # 파싱 결과 유효성 최소 검증
            if(product_details["price"] == None):
                logging.info(f"{main_product_id}의 상품 상세 파싱 실패")
                parser_end = time.perf_counter()
                logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")
                processed_count += 1
                continue

            producer.send('product-details', product_details)

            # 파싱 종료
            parser_end = time.perf_counter()
            logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")
            logging.info(f"[publish] product-details: {product_details}")
            processed_count += 1

    parser.quit()

total_end = time.perf_counter()
logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")