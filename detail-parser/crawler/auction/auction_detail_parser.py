import logging
import re
import time

import undetected_chromedriver as uc
from selenium.common import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait

from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
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

        chrome_binary = CHROME_BINARY
        driver_path = CHROMEDRIVER_PATH

        # 환경변수가 있으면 경로 지정, 없으면 uc가 자동 탐지
        kwargs = {}
        if chrome_binary:
            kwargs["options"] = options
            kwargs["version_main"] = None
            kwargs["driver_executable_path"] = driver_path
            kwargs["options"].binary_location = chrome_binary
            kwargs["use_subprocess"] = True
        else:
            # 자동 탐지용
            kwargs["options"] = options
            kwargs["use_subprocess"] = True
            kwargs["version_main"] = None

        self.driver = uc.Chrome(**kwargs)
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
        try:
            image_url = driver.find_element(By.CSS_SELECTOR, "ul.viewer li.on img").get_attribute('src')
        except NoSuchElementException:
            logging.info(f"[get_product_info] 이미지 파싱 실패")
            image_url = None
        except Exception as e:
            logging.exception(f"[get_product_info] 이미지 파싱 예외 발생: {e}")
            image_url = None

        # 브랜드
        brand_element = driver.find_elements(By.CSS_SELECTOR, "div.box__official-store span.text__brand span.text")
        brand = brand_element[0].text if brand_element else ""

        # 판매자 정보
        try:
            seller = driver.find_element(By.CSS_SELECTOR, "div.box__official-store span.text__seller a.link__seller").text
        except NoSuchElementException:
            logging.info(f"[get_product_info] 판매자 정보 파싱 실패")
            seller = None
        except Exception as e:
            logging.exception(f"[get_product_info] 판매자 정보 파싱 예외 발생: {e}")
            seller = None

        # 제품명
        try:
            name = driver.find_element(By.CSS_SELECTOR, "h1.itemtit").text
        except NoSuchElementException:
            logging.info(f"[get_product_info] 제품명 파싱 실패")
            name = None
        except Exception as e:
            logging.exception(f"[get_product_info] 제품명 파싱 예외 발생: {e}")
            name = None

        # 가격
        try:
            price_txt = driver.find_element(By.CSS_SELECTOR, "div.price strong.price_real").text.replace("판매가", "").replace("원", "").strip()
            price = int(price_txt.replace(",", ""))
        except NoSuchElementException:
            logging.info(f"[get_product_info] 가격 파싱 실패")
            price = None
        except Exception as e:
            logging.exception(f"[get_product_info] 가격 파싱 예외 발생: {e}")
            price = None

        # 배송비
        try:
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
        except NoSuchElementException:
            logging.info(f"[get_product_info] 배송비 파싱 실패")
        except Exception as e:
            logging.exception(f"[get_product_info] 배송비 파싱 예외 발생: {e}")

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