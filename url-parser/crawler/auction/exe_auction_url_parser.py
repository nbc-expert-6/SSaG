from kafka import KafkaProducer
from sqlalchemy import create_engine, MetaData, Table, select
from .auction_url_parser import AuctionUrlParser
from .config import *
import json
import logging

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,  # INFO 이상 레벨만 출력
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.StreamHandler()  # 콘솔 출력
    ]
)

# DB 연결
engine = create_engine(DATABASE_URL)
metadata = MetaData()

p_main_product = Table(
    "p_main_product",
    metadata,
    schema="product_service_db",
    autoload_with=engine
)

with engine.connect() as conn:
    stmt = select(p_main_product.c.id, p_main_product.c.name)
    rows = conn.execute(stmt).mappings().all()

# Kafka Producer 설정
producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# 검색 수행 후 링크 publish
parser = AuctionUrlParser()
for row in rows:
    product_id = str(row['id'])
    keyword = row['name']
    links = parser.get_product_urls(keyword)
    logging.info(f"{keyword} 검색 완료: {len(links)}개 링크")
    producer.send('auction_links', {'id': product_id, 'urls': links})

parser.quit()
producer.flush()
producer.close()
