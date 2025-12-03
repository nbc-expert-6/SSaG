import re
import time
import logging
import undetected_chromedriver as uc

from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from crawler.detail_parser import DetailParser
from common.logging_utils import setup_logger

# Logging 설정
setup_logger()

class AuctionDetailParser(DetailParser):
    def __init__(self):
        options = uc.ChromeOptions()
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        self.driver = uc.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, 10)

    # 제품 상세 페이지 열기
    def open_product_detail_page(self, url: str):
        logging.info(f"[open_product_detail_page] {url}")
        self.driver.get(url)
        time.sleep(2)

    # 제품 상세 정보 수집
    def get_product_info(self) -> dict:
        driver = self.driver

        # 이미지
        image_url = driver.find_element(By.CSS_SELECTOR, "ul.viewer li.on img").get_attribute('src')
        # 브랜드
        brand_element = driver.find_elements(By.CSS_SELECTOR, "div.box__official-store span.text__brand span.text")
        brand = brand_element[0].text if brand_element else ""
        # 판매자 정보
        seller = driver.find_element(By.CSS_SELECTOR, "div.box__official-store span.text__seller a.link__seller").text
        # 제품명
        name = driver.find_element(By.CSS_SELECTOR, "h1.itemtit").text
        # 가격
        price_txt = driver.find_element(By.CSS_SELECTOR, "div.price strong.price_real").text.replace("판매가", "").replace("원", "").strip()
        price = int(price_txt.replace(",", ""))
        # 배송비
        shipping_box = driver.find_element(By.CSS_SELECTOR, "div.box__information-title")
        shipping_texts = shipping_box.find_elements(By.CSS_SELECTOR, "div.box__txt-information > span.text__branch")
        shipping_fee_txt = shipping_texts[0].text if shipping_texts else ""
        if "무료" in shipping_fee_txt:
            shipping_fee = 0
        else:
            match = re.search(r"\(([\d,]+)원\)", shipping_fee_txt)
            if match:
                amount = match.group(1)  # "3,000"
                shipping_fee = int(amount.replace(",", ""))
            else:
                shipping_fee = None

        return {
            "brand": brand,
            "name": name,
            "seller": seller,
            "price": price,
            "shipping_fee": shipping_fee,
            "image_url": image_url
        }

    def get_product_details(self, url: str) -> dict:
        self.open_product_detail_page(url)
        return self.get_product_info()

    def quit(self):
        self.driver.quit()