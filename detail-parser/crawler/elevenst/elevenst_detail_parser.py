import logging
import re
import time

import undetected_chromedriver as uc
from selenium.common import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as ec
from selenium.webdriver.support.ui import WebDriverWait

from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
from common.logging_utils import setup_logger
from common.platform import Platform
from crawler.detail_parser import DetailParser

# Logging 설정
setup_logger()

class ElevenStDetailParser(DetailParser):
    def __init__(self):

        self.main_product_id = None

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

        # 브랜드
        try:
            # 상품 정보 탭으로 이동
            review_tab = self.wait.until(ec.presence_of_element_located((By.ID, "tabMenuDetail1")))
            self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", review_tab)
            time.sleep(0.5)

            self.wait.until(ec.element_to_be_clickable((By.ID, "tabMenuDetail1")))
            review_tab.click()
            time.sleep(1.2)
            logging.info("[get_product_info] 상품 정보 탭 클릭 완료")

            # 브랜드 정보 파싱
            brand_el = driver.find_element(By.XPATH,
                    "//table[contains(@class,'prdc_detail_table')]//th[contains(text(),'브랜드')]/following-sibling::td")
            brand = brand_el.text.strip()
        except NoSuchElementException:
            logging.exception(f"[get_product_info] 브랜드 파싱 실패")
            brand = None
        except Exception as e:
            logging.exception(f"[get_product_info] 브랜드 파싱 예외 발생: {e}")
            brand = None

        # 이미지
        try:
            image_url = driver.find_element(By.CSS_SELECTOR, "div.img_full img").get_attribute('src')
        except NoSuchElementException:
            logging.exception(f"[get_product_info] 이미지 파싱 실패")
            image_url = None
        except Exception as e:
            logging.exception(f"[get_product_info] 이미지 파싱 예외 발생: {e}")
            image_url = None

        # 판매자 정보
        try:
            seller = driver.find_element(By.CSS_SELECTOR, "div.c_product_store_cont h1.c_product_store_title a").text
        except NoSuchElementException:
            logging.exception(f"[get_product_info] 판매자 정보 파싱 실패")
            seller = None
        except Exception as e:
            logging.exception(f"[get_product_info] 판매자 정보 파싱 예외 발생: {e}")
            seller = None

        # 제품명
        try:
            name = driver.find_element(By.CSS_SELECTOR, "div.c_product_info_title h1.title").text
        except NoSuchElementException:
            logging.exception(f"[get_product_info] 제품명 파싱 실패")
            name = None
        except Exception as e:
            logging.exception(f"[get_product_info] 제품명 파싱 예외 발생: {e}")
            name = None

        # 가격
        try:
            price = self._parse_price()
        except Exception as e:
            logging.exception(f"[get_product_info] 가격 파싱 예외 발생: {e}")
            price = None

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
                raise NoSuchElementException("배송 영역 dt 요소 없음")

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

        except NoSuchElementException:
            logging.exception("[get_product_info] 배송비 파싱 실패")
            shipping_fee = None
        except Exception as e:
            logging.exception(f"[get_product_info] 배송비 파싱 예외 발생: {e}")
            shipping_fee = None

        return {
            "main_product_id": self.main_product_id,
            "brand": brand,
            "name": name,
            "seller": seller,
            "price": price,
            "shipping_fee": shipping_fee,
            "image_url": image_url,
            "platform": Platform.ELEVENTH_ST.value,
            "sale_link": self.driver.current_url
        }

    def get_product_details(self, url, main_product_id):
        self.main_product_id = main_product_id
        self.open_product_detail_page(url)
        return self.get_product_info()

    def quit(self):
        self.driver.quit()

    # ------------------------ 내부 메서드 ------------------------

    """
    11번가 가격 파싱
    1) finalDscPrcArea의 노출 가격
    2) maxDiscountResult의 노출 가격
    3) 숨겨진 textContent
    """
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

        return None
