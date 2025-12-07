import logging

from common.database_utils import Database
from common.kafka_utils import create_producer
from common.logging_utils import setup_logger
from crawler.elevenst.elevenst_url_parser import ElevenStUrlParser

# Logging 설정
setup_logger() 

if __name__ == "__main__":

    # DB
    db = Database()
    p_main_product = db.load_table("p_main_product")
    # 데이터 조회
    with db.connect() as conn:
        rows = conn.execute(p_main_product.select()).mappings().all()

    # Kafka Producer
    producer = create_producer()

    # 검색 수행 후 링크 publish
    parser = ElevenStUrlParser()
    for row in rows:
        main_product_id = str(row['id'])
        keyword = row['name']
        urls = parser.get_product_urls(keyword)

        if len(urls) < 1:
            logging.info(f"{keyword} 검색 결과 없음")
            continue

        logging.info(f"{keyword} 검색 완료: {len(urls)}개 링크")
        producer.send('elevenst-product-urls', {'main_product_id': main_product_id, 'urls': urls})

    parser.quit()
    producer.flush()
    producer.close()
