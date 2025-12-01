import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from ..detail_parser import DetailParser

class AuctionDetailParser(DetailParser):
    def __init__(self):
        options = uc.ChromeOptions()
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        self.driver = uc.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, 10)

    def open_product_detail_page(self, url: str):
        self.driver.get(url)

    def get_product_info(self) -> dict:
        driver = self.driver

        # 이미지
        img = driver.find_element(By.CSS_SELECTOR, "ul.viewer li.on img").get_attribute('src')
        # 브랜드
        brand = driver.find_element(By.CSS_SELECTOR, "div.box__official-store span.text__brand span.text").text
        # 판매자 정보
        seller_info = driver.find_element(By.CSS_SELECTOR, "div.box__official-store span.text__seller a.link__seller").text
        # 제품명
        name = driver.find_element(By.CSS_SELECTOR, "h1.itemtit").text
        # 가격
        price = driver.find_element(By.CSS_SELECTOR, "div.price strong.price_real").text.replace("판매가", "").replace("원", "").strip()
        # 배송비
        shipping_box = driver.find_element(By.CSS_SELECTOR, "div.box__information-title")
        shipping_texts = shipping_box.find_elements(By.CSS_SELECTOR, "div.box__txt-information > span.text__branch")
        shipping_fee = shipping_texts[0].text if shipping_texts else ""

        return {
            "image_url": img,
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