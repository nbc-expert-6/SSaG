import os

from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

DATABASE_URL = os.getenv("DATABASE_URL")

TARGET_AUCTION_URL="https://www.auction.co.kr"