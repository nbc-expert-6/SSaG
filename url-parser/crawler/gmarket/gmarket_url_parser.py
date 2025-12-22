import time
from typing import List

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from common.config import (CHROME_BINARY, CHROMEDRIVER_PATH, MAX_LINKS,
                           TARGET_GMARKET_URL)
from crawler.url_parser import UrlParser


class GmarketUrlParser(UrlParser):

    # 생성자 (브라우저, 메인페이지, 상품 파싱 수량 세팅)
    def __init__(self, url=TARGET_GMARKET_URL, max_links=MAX_LINKS):
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

    def search(self, keyword: str):
        search_box = self.wait.until(
            EC.presence_of_element_located((By.ID, "form__search-keyword"))
        )
        search_box.clear()
        search_box.send_keys(keyword)
        search_box.send_keys(Keys.ENTER)
        time.sleep(1.5)

    def has_result(self) -> bool:
        try:
            no_result = self.driver.find_elements(By.CLASS_NAME, "box__component-no-result")
            return len(no_result) == 0
        except Exception:
            return True


    def sort_by_low_price(self):

        # 정렬 열기 버튼 클릭
        toggle_btn = self.wait.until(
            EC.element_to_be_clickable((By.CSS_SELECTOR, "button.button__toggle-sort"))
        )
        toggle_btn.click()

        # 낮은 가격순 버튼 클릭
        low_price_btn = self.wait.until(
            EC.element_to_be_clickable((
                By.XPATH,
                "//span[contains(text(), '낮은 가격순')]/parent::a"
            ))
        )
        low_price_btn.click()

        time.sleep(1.5)

    def remove_add(self):
        pass

    def get_product_links(self) -> List[str]:
        # 상품 카드가 로드될 때까지 대기
        self.wait.until(
            EC.presence_of_all_elements_located(
                (By.CSS_SELECTOR, ".box__item-container a.link__item")
            )
        )

        items = self.driver.find_elements(
            By.CSS_SELECTOR, ".box__item-container a.link__item"
        )

        links = [item.get_attribute("href") for item in items if item.get_attribute("href")]

        # 중복 제거 + 순서 유지
        links = list(dict.fromkeys(links))

        # max_links 만큼 잘라서 반환
        return links[:self.max_links]

    def get_product_urls(self, keyword: str) -> List[str]:
        """
        전체 플로우
        :return: List[str]
        """
        self.open_main_page()
        self.search(keyword)
        if not self.has_result():
            return []
        self.sort_by_low_price()
        self.remove_add()
        links = self.get_product_links()
        return links[:self.max_links]


    def quit(self):
        self.driver.quit()