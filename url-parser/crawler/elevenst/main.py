import logging
import time

from sqlalchemy import func

from common.config import DB_OFFSET, URL_BATCH_SIZE
from common.database_utils import Database
from common.kafka_utils import create_producer
from common.logging_utils import setup_logger
from crawler.elevenst.elevenst_url_parser import ElevenStUrlParser

# [PERF] 전체 코드 실행 start
total_start = time.perf_counter()

# Logging 설정
setup_logger()

if __name__ == "__main__":

    # 한번에 불러올 데이터 크기
    BATCH_SIZE = int(URL_BATCH_SIZE)

    # 인프라 정의
    db = Database()
    p_main_product = db.load_table("p_main_product")
    producer = create_producer()
    parser = ElevenStUrlParser()

    # 처음 시작할 row(env에 정의, 처음부터 하려면 0으로 세팅)
    offset = int(DB_OFFSET)

    # [PERF] 전체 행 수 조회 start
    row_count_start = time.perf_counter()

    # 전체 행 수 조회
    with db.connect() as conn:
        total = conn.execute(
            func.count().select().select_from(p_main_product)
        ).scalar()

    # [PERF] 전체 행 수 조회 end
    row_count_end = time.perf_counter()
    logging.info(f"[PERF] ===== row count time: {row_count_end - row_count_start:.4f}s =====")

    logging.info(f"total rows: {total}")

    while True:
        # [PERF] 데이터 로딩 start
        data_load_start = time.perf_counter()

        # 현재 오프셋부터 배치사이즈만큼 대표상품 로딩
        with db.connect() as conn:
            rows = conn.execute(
                p_main_product.select()
                .offset(offset)
                .limit(BATCH_SIZE)
            ).mappings().all()

        # [PERF] 데이터 로딩 end
        data_load_end = time.perf_counter()
        logging.info(f"[PERF] ===== data load time: {data_load_end - data_load_start:.4f}s =====")

        # 데이터 없으면 중단
        if not rows:
            break

        logging.info(f"current batch offset: {offset}, size: {len(rows)}")

        # 가져온 대표상품을 순회하며 url 파싱
        for row in rows:
            # [PERF] url 파싱 start
            parser_start = time.perf_counter()

            main_product_id = str(row['id'])
            keyword = row['name']

            try:
                urls = parser.get_product_urls(keyword)

                if len(urls) < 1:
                    logging.info(f"{keyword} 검색 결과 없음")

                    # [PERF] url 파싱 end
                    parser_end = time.perf_counter()
                    logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

                    continue

                logging.info(f"{keyword} 검색 완료: {len(urls)}개 링크")
                producer.send(
                    'elevenst-product-urls',
                    {'main_product_id': main_product_id, 'urls': urls}
                )
                # [PERF] url 파싱 end
                parser_end = time.perf_counter()
                logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

            # 실패 지점에서 대표상품 Id와 offset을 로그로 기록
            # 추후 재시도 시 해당 값들 사용
            except Exception as e:
                logging.error(
                    f"처리 실패. main_product_id={main_product_id}, keyword={keyword}, offset={offset}, error={e}",
                    exc_info=True
                )

                # [PERF] url 파싱 end
                parser_end = time.perf_counter()
                logging.info(f"[PERF] ===== parsing time: {parser_end - parser_start:.4f}s =====")

                continue  # 다음 row 진행

        producer.flush()
        logging.info(f"{offset} ~ {offset + len(rows) - 1} 처리 완료")

        offset += BATCH_SIZE

    parser.quit()
    producer.close()

    # [PERF] 전체 코드 실행 end
    total_end = time.perf_counter()
    logging.info(f"[PERF] ===== total time: {total_end - total_start:.4f}s =====")
