import logging

from sqlalchemy import func

from common.config import URL_BATCH_SIZE, DB_OFFSET
from common.database_utils import Database
from common.kafka_utils import create_producer
from common.logging_utils import setup_logger
from crawler.auction.auction_url_parser import AuctionUrlParser

# Logging 설정
setup_logger()

if __name__ == "__main__":

    # 한번에 불러올 데이터 크기
    BATCH_SIZE = int(URL_BATCH_SIZE)

    # 인프라 정의
    db = Database()
    p_main_product = db.load_table("p_main_product")
    producer = create_producer()
    parser = AuctionUrlParser()

    # 처음 시작할 row(env에 정의, 처음부터 하려면 0으로 세팅)
    offset = int(DB_OFFSET)

    # 전체 행 수 조회
    with db.connect() as conn:
        total = conn.execute(
            func.count().select().select_from(p_main_product)
        ).scalar()

    logging.info(f"total rows: {total}")

    while True:

        # 현재 오프셋부터 배치사이즈만큼 대표상품 로딩
        with db.connect() as conn:
            rows = conn.execute(
                p_main_product.select()
                .offset(offset)
                .limit(BATCH_SIZE)
            ).mappings().all()

        # 데이터 없으면 중단
        if not rows:
            break

        logging.info(f"current batch offset: {offset}, size: {len(rows)}")

        # 가져온 대표상품을 순회하며 url 파싱
        for row in rows:
            main_product_id = str(row['id'])
            keyword = row['name']

            try:
                urls = parser.get_product_urls(keyword)

                if len(urls) < 1:
                    logging.info(f"{keyword} 검색 결과 없음")
                    continue

                logging.info(f"{keyword} 검색 완료: {len(urls)}개 링크")
                producer.send(
                    'auction-product-urls',
                    {'main_product_id': main_product_id, 'urls': urls}
                )

            # 실패 지점에서 대표상품 Id와 offset을 로그로 기록
            # 추후 재시도 시 해당 값들 사용
            except Exception as e:
                logging.error(
                    f"처리 실패. main_product_id={main_product_id}, "
                    f"keyword={keyword}, offset={offset}"
                )
                continue  # 다음 row 진행

        producer.flush()
        logging.info(f"{offset} ~ {offset + len(rows) - 1} 처리 완료")

        offset += BATCH_SIZE

    parser.quit()
    producer.close()