import time
import undetected_chromedriver as uc

from common.config import TARGET_GMARKET_URL, MAX_LINKS
from crawler import UrlParser
from typing import List
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

class GmarketUrlParser(UrlParser):

    # 생성자 (브라우저, 메인페이지, 상품 파싱 수량 세팅)
    def __init__(self, url=TARGET_GMARKET_URL, max_links=MAX_LINKS):
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
            EC.presence_of_element_located((By.ID, "form__search-keyword"))
        )
        search_box.clear()
        search_box.send_keys(keyword)
        search_box.send_keys(Keys.ENTER)
        time.sleep(2)

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

        time.sleep(2)

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