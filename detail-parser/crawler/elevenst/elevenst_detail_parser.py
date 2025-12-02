import re
import time
import logging
import undetected_chromedriver as uc

from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as ec
from ..detail_parser import DetailParser
from ...common.logging_utils import setup_logger

# Logging 설정
setup_logger()

class ElevenStDetailParser(DetailParser):
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
        except Exception as e:
            logging.exception(f"[get_product_info] 브랜드 파싱 실패: {e}")
            brand = ""

        # 이미지
        img = driver.find_element(By.CSS_SELECTOR, "div.img_full img").get_attribute('src')
        # 판매자 정보
        seller_info = driver.find_element(By.CSS_SELECTOR, "div.c_product_store_cont h1.c_product_store_title a").text
        # 제품명
        name = driver.find_element(By.CSS_SELECTOR, "div.c_product_info_title h1.title").text
        # 가격
        price = driver.find_element(By.CSS_SELECTOR, "#finalDscPrcArea dd.price .value").text.strip()

        # 배송비
        delivery_dt = driver.find_element(By.CSS_SELECTOR, "div.delivery dt")
        # 텍스트 노드만 추출
        shipping_fee = driver.execute_script("""
            const dt = arguments[0];
            let text = '';
            dt.childNodes.forEach(node => {
                if (node.nodeType === Node.TEXT_NODE) {
                    text += node.textContent;
                }
            });
            return text.trim();
        """, delivery_dt)
        # 괄호 뒤 내용 제거 (예: "배송비 3,000원 (80,000원 이상 무료)")
        if "(" in shipping_fee:
            shipping_fee = shipping_fee.split("(")[0].strip()
        # 금액만 추출
        # shipping_fee = re.sub(r"[^0-9]", "", shipping_raw)

        return {
            "image_url": img,
            "brand": brand,
            "seller_info": seller_info,
            "name": name,
            "price": price,
            "shipping_fee": shipping_fee
        }

    def get_product_details(self, url: str) -> dict:
        self.open_product_detail_page(url)
        return self.get_product_info()

    def quit(self):
        self.driver.quit()
