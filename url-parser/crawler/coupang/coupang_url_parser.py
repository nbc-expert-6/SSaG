import time
import random
from typing import List

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from common.config import (CHROME_BINARY, CHROMEDRIVER_PATH, MAX_LINKS,
                           TARGET_COUPANG_URL)
from crawler.url_parser import UrlParser


class CoupangUrlParser(UrlParser):

    def __init__(self, url=TARGET_COUPANG_URL, max_links=MAX_LINKS):
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

        # Accept / Accept-Language 헤더 위장
        self.driver.execute_cdp_cmd(
            "Network.enable", {}
        )

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

    def search(self, keyword: str):
        search_box = self.wait.until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "input.headerSearchKeyword"))
        )
        search_box.clear()
        search_box.send_keys(keyword)
        search_box.send_keys(Keys.ENTER)
        time.sleep(2)

    """
    검색 결과가 있으면 True, 없으면 False
    """
    def has_result(self) -> bool:

        try:
            # 쿠팡 no-result 요소 탐지
            no_result = self.driver.find_elements(By.CSS_SELECTOR, "[class*='no-result_magnifier']")
            return len(no_result) == 0
        except Exception:
            return False

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

    def get_product_urls(self, keyword: str) -> List[str]:
        """
        전체 플로우
        :return: List[str]
        """
        self.open_main_page()
        self.search(keyword)
        if not self.has_result():
            return []
        time.sleep(random.uniform(0.4, 0.8))
        self.driver.execute_script(
            "window.scrollTo(0, document.body.scrollHeight * 0.4)"
        )
        self.sort_by_low_price()
        self.remove_add()
        links = self.get_product_links()
        return links[:self.max_links]

    def quit(self):
        self.driver.quit()