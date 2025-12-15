import logging
import time

from common.kafka_utils import create_consumer, create_producer
from common.logging_utils import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from common.platform import Platform
from crawler.gmarket.gmarket_detail_parser import GmarketDetailParser

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
    start_metrics_server()

    parser = GmarketDetailParser()

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[gmarket-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 상세 정보 파싱 실행
        for url in urls:
            # 파싱 시작
            parser_start = time.perf_counter()
            CRAWL_TRIAL_COUNT.labels(platform="gmarket").inc()

            try:
                product_details = parser.get_product_details(url)
                product_details["main_product_id"] = main_product_id
                product_details["platform"] = Platform.GMARKET.value

                # 파싱 결과 유효성 최소 검증
                if(product_details["price"] == None):
                    logging.info(f"{main_product_id}의 상품 상세 파싱 실패")
                    parser_end = time.perf_counter()
                    CRAWL_LATENCY.labels(platform="gmarket").observe(parser_end - parser_start)
                    logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")
                    continue

                # 파싱 종료
                parser_end = time.perf_counter()
                CRAWL_LATENCY.labels(platform="gmarket").observe(parser_end - parser_start)
                logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

                # kafka publish
                kafka_start = time.perf_counter()
                producer.send('product-details', product_details)
                kafka_end = time.perf_counter()
                KAFKA_PUBLISH_LATENCY.observe(kafka_end - kafka_start)
                KAFKA_PUBLISH_COUNT.inc()

                logging.info(f"[publish] product-details: {product_details}")

            except Exception as e:
                logging.error(
                    f"처리 실패. main_product_id={main_product_id}, url={url}, error={e}",
                    exc_info=True
                )
                CRAWL_EXCEPTION_COUNT.labels(platform="gmarket").inc()

    parser.quit()

    total_end = time.perf_counter()
    logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")
