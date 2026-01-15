import os

from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP")
CHROME_BINARY = os.getenv("CHROME_BINARY")
CHROMEDRIVER_PATH = os.getenv("CHROMEDRIVER_PATH")
ENVIRONMENT = os.getenv("ENVIRONMENT")

MAX_LINKS = int(os.getenv("MAX_LINKS", "5"))
BATCH_SIZE = os.getenv("BATCH_SIZE", "20")
PLATFORM = os.getenv("PLATFORM")

TARGET_COUPANG_URL = "https://www.coupang.com/"
TARGET_NAVER_STORE_URL = "https://shopping.naver.com/ns/home"
TARGET_AUCTION_URL="https://www.auction.co.kr"
TARGET_11ST_URL="https://search.11st.co.kr"
TARGET_GMARKET_URL="https://www.gmarket.co.kr"