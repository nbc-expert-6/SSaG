import logging
import time

from common.kafka_utils import create_consumer, create_producer
from common.logging_utils import setup_dev_logger
from common.monitoring.logger import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from common.platform import Platform
from crawler.gmarket.gmarket_detail_parser import GmarketDetailParser

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
logger = setup_logger("gmarket")
setup_dev_logger()

if __name__ == "__main__":
    start_metrics_server()

    parser = GmarketDetailParser()
    # 처리한 url 숫자
    # MAX_BATCH로 나누어 떨어질 때 마다 브라우저 리셋
    processed_count = 0
    for url_info in consumer:
        # 받은 url 정보
        logger.info("KAFKA_CONSUME", value=f"{url_info.value}")
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
                    logger.perf("PARSING_COMPLETED", time=round(parser_end - parser_start, 4))
                    processed_count += 1
                    continue

                # 파싱 종료
                parser_end = time.perf_counter()
                CRAWL_LATENCY.labels(platform="gmarket").observe(parser_end - parser_start)
                logger.perf("PARSING_COMPLETED", time=round(parser_end - parser_start, 4))

                # kafka publish
                kafka_start = time.perf_counter()
                producer.send('product-details', product_details)
                kafka_end = time.perf_counter()
                KAFKA_PUBLISH_LATENCY.observe(kafka_end - kafka_start)
                KAFKA_PUBLISH_COUNT.inc()

                logger.info("KAFKA_PUBLISH", product_details=product_details)
                processed_count += 1

            except Exception as e:
                logger.error("PARSING_FAILED",
                             main_product_id=main_product_id,
                             url=url,
                             exception=str(e),
                             exc_info=True)
                CRAWL_EXCEPTION_COUNT.labels(platform="gmarket").inc()

    parser.quit()

    total_end = time.perf_counter()
    logger.info("PROGRAM_EXITED", total_time=round(total_end - total_start, 4))
