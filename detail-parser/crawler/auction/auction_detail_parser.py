import logging
import re
import time

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait

from common.logging_utils import setup_logger
from crawler.detail_parser import DetailParser

# Logging 설정
setup_logger()

class AuctionDetailParser(DetailParser):
    def __init__(self):

        options = uc.ChromeOptions()
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--disable-gpu")
        options.add_argument("--disable-software-rasterizer")
        options.add_argument("--disable-blink-features=AutomationControlled")
        options.add_argument("--disable-extensions")
        options.add_argument("--disable-logging")
        options.add_argument("--disable-web-security")
        options.add_argument("--ignore-certificate-errors")
        options.add_argument("--window-size=1920,1080")

        # Chrome 바이너리 경로 명시 (Debian 계열)
        options.binary_location = "/usr/bin/chromium"

        # ChromeDriver 경로 명시
        driver_executable_path = "/usr/bin/chromedriver"

        try:
            self.driver = uc.Chrome(
                options=options,
                use_subprocess=True,
                driver_executable_path=driver_executable_path,
                version_main=None  # 자동 버전 감지 비활성화
            )
        except Exception as e:
            print(f"Chrome 초기화 실패 (재시도): {e}")
            # 재시도: use_subprocess=False로 시도
            self.driver = uc.Chrome(
                options=options,
                use_subprocess=False,
                driver_executable_path=driver_executable_path,
                version_main=None
            )

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