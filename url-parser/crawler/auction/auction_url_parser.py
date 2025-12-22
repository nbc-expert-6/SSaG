import logging
import time
from typing import List

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from common.config import (CHROME_BINARY, CHROMEDRIVER_PATH, MAX_LINKS,
                           TARGET_AUCTION_URL)
from crawler.url_parser import UrlParser


class AuctionUrlParser(UrlParser):
    def __init__(self, url=TARGET_AUCTION_URL, max_links=MAX_LINKS):
        super().__init__(url, max_links)

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

        # 브라우저 헤더 위장
        options.add_argument(
            "--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/143.0.0.0 Safari/537.36"
        )
        options.add_argument("--lang=ko-KR")

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

        self.driver.execute_cdp_cmd(
            "Network.setExtraHTTPHeaders",
            {
                "headers": {
                    "Accept": (
                        "text/html,application/xhtml+xml,application/xml;"
                        "q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
                    ),
                    "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8"
                }
            }
        )

    def open_main_page(self):
        self.driver.get(self.url)
        time.sleep(1)

    def search(self, keyword: str):
        try:
            search_box = self.wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "input.search_input_keyword")))
            search_box.clear()
            search_box.send_keys(keyword)
            search_box.send_keys(Keys.ENTER)
            time.sleep(0.7)
        except:
            logging.info(self.driver.page_source)

    def sort_by_low_price(self):
        try:
            sort_button = self.wait.until(EC.presence_of_element_located(
                (By.CSS_SELECTOR, "button.button--toggle_sort_item_list")))
            self.driver.execute_script("arguments[0].click();", sort_button)
            time.sleep(0.7)

            low_price_link = self.wait.until(EC.presence_of_element_located(
                (By.XPATH, "//ul[@class='list']/li[3]/a[@class='link']")))
            self.driver.execute_script("arguments[0].click();", low_price_link)
            time.sleep(0.7)
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

    def has_no_result(self) -> bool:
        try:
            # 검색 결과 없음 영역 존재 확인
            self.wait.until(
                EC.presence_of_element_located(
                    (By.CSS_SELECTOR, "div.component.component--no_result")
                )
            )
            return True
        except:
            return False

    def get_product_urls(self, keyword: str) -> List[str]:
        self.open_main_page()
        self.search(keyword)

        if self.has_no_result():
            return []

        self.sort_by_low_price()
        self.remove_add()

        links = self.get_product_links()
        return links[:self.max_links]

    def quit(self):
        try:
            self.driver.quit()
        except Exception as e:
            print(f"Driver 종료 중 오류: {e}")