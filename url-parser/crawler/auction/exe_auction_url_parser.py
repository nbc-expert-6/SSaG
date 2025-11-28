from ...common.database_utils import Database
from ...common.logging_utils import setup_logger
from ...common.kafka_utils import create_producer
from .auction_url_parser import AuctionUrlParser
from ...common.config import *
import logging

# Logging 설정
setup_logger()

# DB
db = Database(DATABASE_URL)
p_main_product = db.load_table("p_main_product", schema="product_service_db")
# 데이터 조회
with db.connect() as conn:
    rows = conn.execute(p_main_product.select()).mappings().all()

# Kafka Producer
producer = create_producer()

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
