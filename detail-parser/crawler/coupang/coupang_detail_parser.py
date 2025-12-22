import re
import time

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait

from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
from common.exceptions import DetailParseException
from crawler.detail_parser import DetailParser


class CoupangDetailParser(DetailParser):
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

    def open_product_detail_page(self, url: str):
        # print(f"[INFO] 상품 페이지 오픈: {url}")
        try:
            self.driver.get(url)
            time.sleep(2)
        except Exception as e:
            raise DetailParseException(
                stage="page_load",
                reason="failed to load product page",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

    def get_product_info(self) -> dict:
        info = {}

        # 판매링크와 플랫폼 세팅
        info["sale_link"] = self.driver.current_url

        # 브랜드
        try:
            brand_elem = self.driver.find_element(By.CSS_SELECTOR, "div.twc-text-sm.twc-text-blue-600")
            info["brand"] = brand_elem.text.strip()
        except Exception as e:
            raise DetailParseException(
                stage="brand",
                reason="brand parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 상품명
        try:
            title_elem = self.driver.find_element(By.CSS_SELECTOR, "h1.product-title span")
            info["name"] = title_elem.text.strip()
        except Exception as e:
            raise DetailParseException(
                stage="name",
                reason="product name parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 가격
        try:
            price_elem = self.driver.find_element(
                By.CSS_SELECTOR,
                "div.price-amount.final-price-amount"
            )
            raw_price = price_elem.text.strip()  # 예: "35,900원"

            # 숫자만 추출
            numeric_price = int(re.sub(r"[^0-9]", "", raw_price))

            info["price"] = numeric_price
        except Exception as e:
            raise DetailParseException(
                stage="price",
                reason="price parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 배송비
        try:
            # 무료배송
            free_shipping = self.driver.find_elements(
                By.CSS_SELECTOR,
                "span.extreme-prominence-shipping-fee-txt"
            )
            if free_shipping:
                info["shipping_fee"] = 0
            else:
                # 배송비 금액
                fee_elem = self.driver.find_elements(
                    By.CSS_SELECTOR,
                    "div.price-shipping-fee-info-container div.twc-ml-\\[3px\\]"
                )
                if fee_elem:
                    raw_fee = fee_elem[0].text.strip()  # e.g. "15,000원"
                    # 숫자만 추출
                    numeric_fee = int(re.sub(r"[^0-9]", "", raw_fee))
                    info["shipping_fee"] = numeric_fee
                else:
                    info["shipping_fee"] = None
                    # print("[WARN] 배송비 정보를 찾을 수 없음")
        except Exception as e:
            raise DetailParseException(
                stage="shipping_fee",
                reason="shipping fee parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 판매자 이름
        try:
            seller_elem = self.driver.find_element(
                By.CSS_SELECTOR,
                "div.twc-flex.twc-flex-row.twc-justify-start.twc-items-center.twc-flex-wrap a"
            )

            # 첫 번째 텍스트 노드만 가져오기
            seller_name = self.driver.execute_script(
                "return arguments[0].childNodes[0].textContent.trim();",
                seller_elem
            )

            info['seller'] = seller_name
        except Exception as e:
            raise DetailParseException(
                stage="seller",
                reason="seller parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 이미지
        try:
            img_elem = self.driver.find_element(By.CSS_SELECTOR, "div.twc-relative img")
            img_url = img_elem.get_attribute("src")

            if img_url.startswith("//"):
                img_url = "https:" + img_url

            info["image_url"] = img_url
        except Exception as e:
            raise DetailParseException(
                stage="image",
                reason="image parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # print(info)
        return info

    def get_product_details(self, url: str) -> dict:
        # print("[INFO] 상세 정보 크롤링 시작")
        self.open_product_detail_page(url)
        info = self.get_product_info()
        # print("[INFO] 상세 정보 크롤링 완료")
        return info

    def quit(self):
        self.driver.quit()
