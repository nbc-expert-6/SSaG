import re
import time

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait

from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
from common.exceptions import DetailParseException
from crawler.detail_parser import DetailParser


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
            kwargs["driver_executable_path"] = driver_path
            kwargs["options"].binary_location = chrome_binary
            kwargs["version_main"] = None
            kwargs["use_subprocess"] = True
        else:
            kwargs["options"] = options
            kwargs["version_main"] = None
            kwargs["use_subprocess"] = True

        self.driver = uc.Chrome(**kwargs)
        self.wait = WebDriverWait(self.driver, 10)

    # 제품 상세 페이지 열기
    def open_product_detail_page(self, url: str):
        try:
            self.driver.get(url)
            time.sleep(1.5)
        except Exception as e:
            raise DetailParseException(
                stage="page_load",
                reason="failed to load product page",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

    # 제품 상세 정보 수집
    def get_product_info(self) -> dict:
        driver = self.driver

        # 이미지
        try:
            image_url = driver.find_element(
                By.CSS_SELECTOR, "ul.viewer li.on img"
            ).get_attribute("src")
        except Exception as e:
            raise DetailParseException(
                stage="image",
                reason="image parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 브랜드
        try:
            brand_elements = driver.find_elements(
                By.CSS_SELECTOR,
                "div.box__official-store span.text__brand span.text",
            )
            brand = brand_elements[0].text if brand_elements else ""
        except Exception as e:
            raise DetailParseException(
                stage="brand",
                reason="brand parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 판매자 정보
        try:
            seller = driver.find_element(
                By.CSS_SELECTOR,
                "div.box__official-store span.text__seller a.link__seller",
            ).text
        except Exception as e:
            raise DetailParseException(
                stage="seller",
                reason="seller parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 제품명
        try:
            name = driver.find_element(By.CSS_SELECTOR, "h1.itemtit").text
        except Exception as e:
            raise DetailParseException(
                stage="name",
                reason="product name parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 가격
        try:
            price_txt = (
                driver.find_element(By.CSS_SELECTOR, "div.price strong.price_real")
                .text.replace("판매가", "")
                .replace("원", "")
                .strip()
            )
            price = int(price_txt.replace(",", ""))
        except Exception as e:
            raise DetailParseException(
                stage="price",
                reason="price parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 배송비
        try:
            shipping_box = driver.find_element(
                By.CSS_SELECTOR, "div.box__information-title"
            )
            shipping_texts = shipping_box.find_elements(
                By.CSS_SELECTOR, "div.box__txt-information > span.text__branch"
            )
            shipping_fee_txt = shipping_texts[0].text if shipping_texts else ""

            if "무료" in shipping_fee_txt:
                shipping_fee = 0
            else:
                match = re.search(r"\(([\d,]+)원\)", shipping_fee_txt)
                shipping_fee = (
                    int(match.group(1).replace(",", "")) if match else None
                )
        except Exception as e:
            raise DetailParseException(
                stage="shipping_fee",
                reason="shipping fee parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        return {
            "brand": brand,
            "name": name,
            "seller": seller,
            "price": price,
            "shipping_fee": shipping_fee,
            "image_url": image_url,
            "sale_link": self.driver.current_url
        }

    def get_product_details(self, url: str) -> dict:
        self.open_product_detail_page(url)
        return self.get_product_info()

    def quit(self):
        self.driver.quit()
