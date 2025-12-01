import time
import undetected_chromedriver as uc
from crawler import UrlParser
from typing import List
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class CoupangUrlParser(UrlParser):

    def __init__(self, url, max_links):
        super().__init__(url, max_links)

        options = uc.ChromeOptions()
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        self.driver = uc.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, 10)

    def open_main_page(self):
        self.driver.get(self.url)

    def search(self, keyword: str):
        search_box = self.wait.until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "input.headerSearchKeyword"))
        )
        search_box.clear()
        search_box.send_keys(keyword)
        search_box.send_keys(Keys.ENTER)
        time.sleep(2)

    def sort_by_low_price(self):
        low_price_btn = self.wait.until(
            EC.element_to_be_clickable((By.CSS_SELECTOR, "label[for='sorter-LOW_PRICE']"))
        )
        low_price_btn.click()
        time.sleep(3)

    def remove_add(self):
        # 쿠팡 광고 제거 로직 필요하면 구현
        pass

    def get_product_links(self) -> List[str]:
        product_lis = self.driver.find_elements(By.CSS_SELECTOR, "li.ProductUnit_productUnit__Qd6sv")
        links = []
        for li in product_lis[:self.max_links]:
            a_tag = li.find_element(By.TAG_NAME, "a")
            links.append(a_tag.get_attribute("href"))
        return links