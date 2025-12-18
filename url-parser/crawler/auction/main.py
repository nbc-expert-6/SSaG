import logging
import time

from common.config import BATCH_SIZE
from common.kafka_utils import create_producer, create_consumer
from common.logging_utils import setup_dev_logger
from common.monitoring.logger import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT,
                                       KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from crawler.auction.auction_url_parser import AuctionUrlParser

# 해당 크기만큼 처리 후 브라우저 리셋
MAX_BATCH = int(BATCH_SIZE)

# 시작 시간
total_start = time.perf_counter()

# Kafka Consumer 설정
consumer = create_consumer(
    topic='keywords',
    group_id='auction-url-group'
)

producer = create_producer()

# Logging 설정
setup_dev_logger()
logger = setup_logger("auction")

if __name__ == "__main__":
    start_metrics_server()

    parser = AuctionUrlParser()

    # MAX_BATCH로 나누어 떨어질 때 마다 브라우저 리셋
    processed_count = 0

    # 가져온 대표상품을 순회하며 url 파싱
    for item in consumer:

        logger.info("KAFKA_CONSUME", value=f"{item.value}")
        main_product_id = item.value['main_product_id']
        keyword = item.value['name']

        if processed_count > 0 and processed_count % MAX_BATCH == 0:
            logging.info(f"브라우저 리셋: {processed_count}건 처리됨, 새 Chrome 시작")
            parser.quit()
            parser = AuctionUrlParser()
            time.sleep(1)

        # 파싱 시작 시간
        parser_start = time.perf_counter()
        CRAWL_TRIAL_COUNT.labels(platform="auction").inc()

        try:
            urls = parser.get_product_urls(keyword)

            if len(urls) < 1:
                logger.info("SEARCH_NO_RESULT", keyword=str(keyword))

                parser_end = time.perf_counter()
                logger.perf("PARSING_COMPLETED", url_cnt=len(urls), time=round(parser_end - parser_start, 4))
                CRAWL_LATENCY.labels(platform="auction").observe(parser_end - parser_start)
                processed_count += 1
                consumer.commit()
                continue

            logger.info("SEARCH_COMPLETED", keyword=str(keyword), url_cnt=len(urls))

            publish_start = time.perf_counter()
            producer.send(
                'auction-product-urls',
                {'main_product_id': main_product_id, 'urls': urls}
            )
            publish_end = time.perf_counter()
            KAFKA_PUBLISH_LATENCY.observe(publish_end - publish_start)
            KAFKA_PUBLISH_COUNT.inc()

            # 파싱 종료 시간
            parser_end = time.perf_counter()
            logger.perf("PARSING_COMPLETED", url_cnt=len(urls), time=round(parser_end - parser_start, 4))
            processed_count += 1
            consumer.commit()
        # 실패 지점에서 대표상품 Id와 offset을 로그로 기록
        # 추후 재시도 시 해당 값들 사용
        except Exception as e:
            logger.error("PARSING_FAILED",
                         main_product_id=main_product_id,
                         keyword=str(keyword),
                         exception=str(e),
                         exc_info=True)

            # 파싱 종료 시간
            parser_end = time.perf_counter()
            logger.perf("PARSING_COMPLETED", time=round(parser_end - parser_start, 4))
            CRAWL_LATENCY.labels(platform="auction").observe(parser_end - parser_start)
            CRAWL_EXCEPTION_COUNT.labels(platform="auction").inc()
            consumer.commit()

            continue  # 다음 row 진행


    parser.quit()
producer.close()

total_end = time.perf_counter()
logger.info("PROGRAM_EXITED", total_time=round(total_end - total_start, 4))
