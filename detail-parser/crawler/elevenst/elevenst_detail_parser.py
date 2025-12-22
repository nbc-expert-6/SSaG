import re
import time

import undetected_chromedriver as uc
from selenium.common import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as ec
from selenium.webdriver.support.ui import WebDriverWait

from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
from common.exceptions import DetailParseException
from crawler.detail_parser import DetailParser


class ElevenStDetailParser(DetailParser):
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

        # 브랜드
        try:
            # 상품 정보 탭으로 이동
            review_tab = self.wait.until(ec.presence_of_element_located((By.ID, "tabMenuDetail1")))
            self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", review_tab)
            time.sleep(0.5)

            self.wait.until(ec.element_to_be_clickable((By.ID, "tabMenuDetail1")))
            review_tab.click()
            time.sleep(1)

            # 브랜드 정보 파싱
            brand_el = driver.find_element(By.XPATH,
                    "//table[contains(@class,'prdc_detail_table')]//th[contains(text(),'브랜드')]/following-sibling::td")
            brand = brand_el.text.strip()
        except Exception as e:
            raise DetailParseException(
                stage="brand",
                reason="brand parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 이미지
        try:
            image_url = driver.find_element(By.CSS_SELECTOR, "div.img_full img").get_attribute('src')
        except Exception as e:
            raise DetailParseException(
                stage="image",
                reason="image parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 판매자 정보
        try:
            seller = driver.find_element(By.CSS_SELECTOR, "div.c_product_store_cont h1.c_product_store_title a").text
        except Exception as e:
            raise DetailParseException(
                stage="seller",
                reason="seller parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 제품명
        try:
            name = driver.find_element(By.CSS_SELECTOR, "div.c_product_info_title h1.title").text
        except Exception as e:
            raise DetailParseException(
                stage="name",
                reason="product name parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 가격
        try:
            price = self._parse_price()
        except Exception as e:
            raise DetailParseException(
                stage="price",
                reason="price parsing failed",
                original_exception=e,
                original_exception_type=type(e).__name__,
            )

        # 배송비
        try:
            # delivery / delivery_abroad 중 하나 선택
            delivery_dt = None
            for selector in ["div.delivery dt", "div.delivery_abroad dt"]:
                elements = driver.find_elements(By.CSS_SELECTOR, selector)
                if elements:
                    delivery_dt = elements[0]
                    break

            if not delivery_dt:
                raise NoSuchElementException("shipping fee element not found")

            # 텍스트 추출: 텍스트 노드 + 버튼/스팬 제거 후 텍스트만
            shipping_fee_txt = driver.execute_script("""
                const dt = arguments[0];
                let output = '';
                dt.childNodes.forEach(node => {
                    if (node.nodeType === Node.TEXT_NODE) {
                        output += node.textContent;
                    }
                });
                return output.trim();
            """, delivery_dt)

            # 예: "무료배송  (조건부 무료)" → 괄호 제거
            if "(" in shipping_fee_txt:
                shipping_fee_txt = shipping_fee_txt.split("(")[0].strip()

            # 무료배송 처리
            if "무료" in shipping_fee_txt:
                shipping_fee = 0
            else:
                # 숫자 패턴 탐색
                match = re.search(r'(\d{1,3}(?:,\d{3})*|\d+)원', shipping_fee_txt)
                if match:
                    shipping_fee = int(match.group(1).replace(',', ''))
                else:
                    shipping_fee = None
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

    def get_product_details(self, url):
        self.open_product_detail_page(url)
        return self.get_product_info()

    def quit(self):
        self.driver.quit()

    # ------------------------ 내부 메서드 ------------------------

    def _parse_price(self):
        # finalDscPrcArea
        try:
            elem = self.driver.find_element(By.CSS_SELECTOR, "#finalDscPrcArea dd.price .value")
            visible_price = elem.text.strip()
            if visible_price:
                return int(visible_price.replace(",", ""))
        except NoSuchElementException:
            pass

        # maxDiscountResult
        try:
            elem = self.driver.find_element(By.CSS_SELECTOR, "#maxDiscountResult dd.price .value")
            visible_discount = elem.text.strip()
            if visible_discount:
                return int(visible_discount.replace(",", ""))
        except NoSuchElementException:
            pass

        # finalDscPrcArea의 textContent
        try:
            elem = self.driver.find_element(By.CSS_SELECTOR, "#finalDscPrcArea dd.price .value")
            hidden = elem.get_attribute("textContent").strip()
            if hidden:
                return int(hidden.replace(",", ""))
        except NoSuchElementException:
            pass

        raise NoSuchElementException("price element not found")
