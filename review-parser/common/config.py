import os

from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

PLATFORM = os.getenv("PLATFORM")
KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP")
CHROME_BINARY = os.getenv("CHROME_BINARY")
CHROMEDRIVER_PATH = os.getenv("CHROMEDRIVER_PATH")
ENVIRONMENT = os.getenv("ENVIRONMENT")
