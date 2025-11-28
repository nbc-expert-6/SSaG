from sqlalchemy import create_engine, MetaData, Table, select
from .auction_url_parser import AuctionUrlParser
from .config import *

# DB 연결
engine = create_engine(DATABASE_URL)
metadata = MetaData()

p_main_product = Table(
    "p_main_product",
    metadata,
    schema="product_service_db",
    autoload_with=engine
)

# DB에서 키워드 가져오기
with engine.connect() as conn:
    stmt = select(p_main_product)
    rows = conn.execute(stmt).mappings().all()

keyword_list = [row["name"] for row in rows]

# 검색 수행
parser = AuctionUrlParser()
for keyword in keyword_list:
    links = parser.get_product_urls(keyword)
    print(f"{keyword} 검색 완료: {len(links)}개 링크")
    for link in links:
        print(link)
    print("-" * 50)

parser.quit()
