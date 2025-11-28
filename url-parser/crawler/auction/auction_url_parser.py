import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
from typing import List
from ..url_parser import UrlParser
from .config import *

class AuctionUrlParser(UrlParser):
    def __init__(self, url=TARGET_AUCTION_URL, max_links=MAX_LINKS):
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
        search_box = self.wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "input.search_input_keyword")))
        search_box.clear()
        search_box.send_keys(keyword)
        search_box.send_keys(Keys.ENTER)
        time.sleep(3)

    def sort_by_low_price(self):
        try:
            sort_button = self.wait.until(EC.presence_of_element_located(
                (By.CSS_SELECTOR, "button.button--toggle_sort_item_list")))
            self.driver.execute_script("arguments[0].click();", sort_button)
            time.sleep(2)

            low_price_link = self.wait.until(EC.presence_of_element_located(
                (By.XPATH, "//ul[@class='list']/li[3]/a[@class='link']")))
            self.driver.execute_script("arguments[0].click();", low_price_link)
            time.sleep(3)
        except Exception as e:
            print(f"정렬 실패: {e}")

    def remove_add(self):
        pass

    def get_product_links(self) -> List[str]:
        product_cards = self.driver.find_elements(By.CSS_SELECTOR, "div.section--itemcard_img a")
        links = []
        for a_tag in product_cards[:self.max_links]:
            href = a_tag.get_attribute("href")
            if href:
                links.append(href)
        return links

    def get_product_urls(self, keyword: str) -> List[str]:
        self.open_main_page()
        self.search(keyword)
        self.sort_by_low_price()
        self.remove_add()
        links = self.get_product_links()
        return links[:self.max_links]

    def quit(self):
        self.driver.quit()
