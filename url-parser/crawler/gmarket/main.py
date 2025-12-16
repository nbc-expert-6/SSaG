import logging
import time

from sqlalchemy import func

from common.config import DB_OFFSET, URL_BATCH_SIZE
from common.database_utils import Database
from common.kafka_utils import create_producer
from common.logging_utils import setup_dev_logger
from common.monitoring.logger import setup_logger
from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, DB_QUERY_LATENCY,
                                       KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)
from common.monitoring.metrics_server import start_metrics_server
from crawler.gmarket.gmarket_url_parser import GmarketUrlParser

# 시작 시간
total_start = time.perf_counter()

# Logging 설정
setup_dev_logger()
logger = setup_logger("gmarket")

if __name__ == "__main__":
    start_metrics_server()

    # 한번에 불러올 데이터 크기
    BATCH_SIZE = int(URL_BATCH_SIZE)

    # 인프라 정의
    db = Database()
    p_main_product = db.load_table("p_main_product")
    producer = create_producer()
    parser = GmarketUrlParser()

    # 처음 시작할 row(env에 정의, 처음부터 하려면 0으로 세팅)
    offset = int(DB_OFFSET)

    # row 측정 io 시작 시간
    row_count_start = time.perf_counter()

    # 전체 행 수 조회
    with db.connect() as conn:
        total = conn.execute(
            func.count().select().select_from(p_main_product)
        ).scalar()
    # row 측정 io 종료 시간
    row_count_end = time.perf_counter()
    logger.perf("DB_QUERY_COMPLETED", query_type="total_count", time=round(row_count_end - row_count_start, 4))
    DB_QUERY_LATENCY.observe(row_count_end - row_count_start)

    logger.info("TOTAL_ROWS_RETRIEVED", total_rows=total)

    while True:

        # 배치 사이즈 만큼 로딩 시작 시간
        data_load_start = time.perf_counter()

        # 현재 오프셋부터 배치사이즈만큼 대표상품 로딩
        with db.connect() as conn:
            rows = conn.execute(
                p_main_product.select()
                .offset(offset)
                .limit(BATCH_SIZE)
            ).mappings().all()

        # 배치 사이즈 만큼 로딩 종료 시간
        data_load_end = time.perf_counter()
        logger.perf("DB_QUERY_COMPLETED", query_type="batch_load",
                    offset=offset, time=round(data_load_end - data_load_start, 4))
        DB_QUERY_LATENCY.observe(data_load_end - data_load_start)

        # 새로운 오프셋을 불러올 때 브라우저 초기화
        logging.info("브라우저 리셋, 새 브라우저 로딩")
        parser.quit()
        parser = GmarketUrlParser()
        time.sleep(1)

        # 데이터 없으면 중단
        if not rows:
            break

        logger.info("BATCH_STARTED", offset=offset, batch_size=len(rows))

        # 가져온 대표상품을 순회하며 url 파싱
        for row in rows:
            # 파싱 시작 시간
            parser_start = time.perf_counter()

            main_product_id = str(row['id'])
            keyword = row['name']

            CRAWL_TRIAL_COUNT.labels(platform="auction").inc()

            try:
                urls = parser.get_product_urls(keyword)

                if len(urls) < 1:
                    logger.info("SEARCH_NO_RESULT", keyword=str(keyword))

                    parser_end = time.perf_counter()
                    logger.perf("PARSING_COMPLETED", url_cnt=len(urls), time=round(parser_end - parser_start, 4))
                    CRAWL_LATENCY.labels(platform="gmarket").observe(parser_end - parser_start)

                    continue

                logger.info("SEARCH_COMPLETED", keyword=str(keyword), url_cnt=len(urls))

                publish_start = time.perf_counter()
                producer.send(
                    'gmarket-product-urls',
                    {'main_product_id': main_product_id, 'urls': urls}
                )
                publish_end = time.perf_counter()
                KAFKA_PUBLISH_LATENCY.observe(publish_end - publish_start)
                KAFKA_PUBLISH_COUNT.inc()

                # 파싱 종료 시간
                parser_end = time.perf_counter()
                logger.perf("PARSING_COMPLETED", url_cnt=len(urls), time=round(parser_end - parser_start, 4))

            # 실패 지점에서 대표상품 Id와 offset을 로그로 기록
            # 추후 재시도 시 해당 값들 사용
            except Exception as e:
                logger.error("PARSING_FAILED",
                             main_product_id=main_product_id,
                             keyword=str(keyword),
                             offset=offset,
                             exception=str(e),
                             exc_info=True)

                # 파싱 종료 시간
                parser_end = time.perf_counter()
                logger.perf("PARSING_COMPLETED", time=round(parser_end - parser_start, 4))
                CRAWL_LATENCY.labels(platform="gmarket").observe(parser_end - parser_start)
                CRAWL_EXCEPTION_COUNT.labels(platform="gmarket").inc()

                continue  # 다음 row 진행

        producer.flush()
        logger.info("OFFSET_PROCESSED", range=f"{offset} ~ {offset + len(rows) - 1}")

        offset += BATCH_SIZE

    parser.quit()
    producer.close()

    total_end = time.perf_counter()
    logger.info("PROGRAM_EXITED", total_time=round(total_end - total_start, 4))
