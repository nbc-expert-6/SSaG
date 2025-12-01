import time
from common.logging_utils import setup_logger
from crawler.gmarket.gmarket_url_parser import GmarketUrlParser

setup_logger()

# 테스트할 검색 키워드 리스트
keyword = "나이키 에어포스 cw2288-111"

parser = GmarketUrlParser("https://www.gmarket.co.kr",10)

links = parser.get_product_urls(keyword)

for link in links:
    print(link)