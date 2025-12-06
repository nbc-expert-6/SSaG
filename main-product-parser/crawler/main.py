import time
import uuid

from datetime import datetime
from common.database_utils import Database
from sqlalchemy import insert

from crawler.main_product_parser import MainProductParser

db = Database()
conn = db.connect()
table = db.load_table("p_main_product")

parser = MainProductParser()
time.sleep(3)
parser.open_main_page()
dict = parser.parse_all_categories()


allow = {"브랜드 여성의류", "브랜드 남성의류", "브랜드 잡화"}

filtered = {k: v for k, v in dict.items() if k in allow}

for depth2, depth3_map in filtered.items():
    for depth3, url in depth3_map.items():
        items = parser.parse_products(url, depth2, depth3)
        # 파싱 수량
        print(f"[{depth2}] {depth3} 상품 개수: {len(items)}")
        for item in items:
            stmt = insert(table).values(
                id=str(uuid.uuid4()),
                category_medium_id=item["category_medium_id"],
                brand=item["brand"],
                name=item["name"],
                image_url=item.get("image_url"),
                click_count = 0,
                review_count = 0,
                review_rating_avg = 0,
                lowest_price = 10000000,
                created_at=datetime.now(),
                updated_at=datetime.now(),
                deleted_at=None
            )
            conn.execute(stmt)
            conn.commit()

conn.close()

