import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
from typing import List
from crawler.url_parser import UrlParser
from common.config import *

class ElevenStUrlParser(UrlParser):
    def __init__(self, url=TARGET_11ST_URL, max_links=MAX_LINKS):
        super().__init__(url, max_links)

        options = uc.ChromeOptions()
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        self.driver = uc.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, 10)

    def open_main_page(self):
        self.driver.get(self.url)
        time.sleep(1)

    def search(self, keyword: str):
        search_box = self.wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "input.search_text")))
        search_box.clear()
        search_box.send_keys(keyword)
        search_box.send_keys(Keys.ENTER)
        time.sleep(2)

    def sort_by_low_price(self):
        try:
            # 드롭다운 열기
            sort_button = self.wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "button.btn_icon.select")))
            sort_button.click()
            time.sleep(1)

            # 낮은 가격순 정렬 산텍
            low_price_btn = self.wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "button[data-log-body*='낮은 가격순']")))
            low_price_btn.click()
            time.sleep(3)

        except Exception as e:
            print(f"정렬 실패: {e}")

    def remove_add(self):
        pass

    def get_product_links(self) -> List[str]:
        product_cards = self.driver.find_elements(By.CSS_SELECTOR, "div.c-card-item.c-card-item--list")
        links = []
        for card in product_cards[:self.max_links]:
            a_tag = card.find_element(By.CSS_SELECTOR, "a.c-card-item__anchor")
            href = a_tag.get_attribute("href")
            links.append(href)
        return links

    def quit(self):
        self.driver.quit()
