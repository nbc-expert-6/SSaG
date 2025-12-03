import re
import time
import logging
import undetected_chromedriver as uc

from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as ec
from crawler.detail_parser import DetailParser
from common.logging_utils import setup_logger

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
        image_url = driver.find_element(By.CSS_SELECTOR, "div.img_full img").get_attribute('src')
        # 판매자 정보
        seller = driver.find_element(By.CSS_SELECTOR, "div.c_product_store_cont h1.c_product_store_title a").text
        # 제품명
        name = driver.find_element(By.CSS_SELECTOR, "div.c_product_info_title h1.title").text
        # 가격
        price_txt = driver.find_element(By.CSS_SELECTOR, "#finalDscPrcArea dd.price .value").text.strip()
        price = int(price_txt.replace(",", ""))
        # 배송비
        delivery_dt = driver.find_element(By.CSS_SELECTOR, "div.delivery dt")
        # 텍스트 노드만 추출
        shipping_fee_txt = driver.execute_script("""
            const dt = arguments[0];
            let text = '';
            dt.childNodes.forEach(node => {
                if (node.nodeType === Node.TEXT_NODE) {
                    text += node.textContent;
                }
            });
            return text.trim();
        """, delivery_dt)

        # 배송비 숫자 추출
        if "(" in shipping_fee_txt:
            shipping_fee_txt = shipping_fee_txt.split("(")[0].strip()
        if "무료" in shipping_fee_txt:
            shipping_fee = 0
        else:
            match = re.search(r'(\d{1,3}(?:,\d{3})*|\d+)원', shipping_fee_txt)
            if match:
                shipping_fee = int(match.group(1).replace(',', ''))
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
