import time

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from common.platform import Platform
from crawler.detail_parser import DetailParser


class GmarketDetailParser(DetailParser):

    def __init__(self):
        """
        :param image_save_dir: 이미지 저장 경로
        """
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

        # Chrome 바이너리 경로 명시 (Debian 계열)
        options.binary_location = "/usr/bin/chromium"

        # ChromeDriver 경로 명시
        driver_executable_path = "/usr/bin/chromedriver"

        try:
            self.driver = uc.Chrome(
                options=options,
                use_subprocess=True,
                driver_executable_path=driver_executable_path,
                version_main=None  # 자동 버전 감지 비활성화
            )
        except Exception as e:
            print(f"Chrome 초기화 실패 (재시도): {e}")
            # 재시도: use_subprocess=False로 시도
            self.driver = uc.Chrome(
                options=options,
                use_subprocess=False,
                driver_executable_path=driver_executable_path,
                version_main=None
            )

        self.wait = WebDriverWait(self.driver, 10)

    def open_product_detail_page(self, url: str):
        self.driver.get(url)
        time.sleep(1)

    def get_product_info(self) -> dict:
        info = {}


        # 브랜드
        try:
            brand_elem = self.wait.until(EC.presence_of_element_located(
                (By.CSS_SELECTOR, "span.text__brand span.text")
            ))
            info["brand"] = brand_elem.text.strip()
        except:
            info["brand"] = None

        # 판매자
        try:
            seller_elem = self.wait.until(EC.presence_of_element_located(
                (By.CSS_SELECTOR, "span.text__seller a.link__seller")
            ))
            info["seller"] = seller_elem.text.strip()
        except:
            info["seller"] = None

        # 가격 (쿠폰 적용가 우선)
        coupon_price = self.driver.find_elements(
            By.CSS_SELECTOR, ".price_innerwrap.price_innerwrap-coupon strong.price_real"
        )

        if coupon_price:
            price_text = coupon_price[0].text.replace(",", "").replace("원", "").strip()
            info["price"] = int(price_text)
        else:
            normal_price = self.driver.find_elements(
                By.CSS_SELECTOR, ".price_innerwrap:not(.price_innerwrap-coupon) strong.price_real"
            )
            if normal_price:
                price_text = normal_price[0].text.replace(",", "").replace("원", "").strip()
                info["price"] = int(price_text)
            else:
                info["price"] = None

        # 배송비 처리
        info["shipping_fee"] = None
        delivery_elems = self.driver.find_elements(By.CSS_SELECTOR, "div.box__txt-information span.text__branch")

        for elem in delivery_elems:
            text = elem.text.strip()

            # 무료배송 처리
            if "무료배송" in text:
                info["shipping_fee"] = 0
                break

            # 배송비 있는 경우 처리
            elif "배송비" in text:
                # 숫자만 추출
                import re
                match = re.search(r"\d+", text.replace(",", ""))
                if match:
                    info["shipping_fee"] = int(match.group())
                break


        # 이미지 링크
        try:
            li_elem = self.wait.until(EC.presence_of_element_located(
                (By.CSS_SELECTOR, "ul.viewer li.on img")
            ))
            img_url = li_elem.get_attribute("src")

            # //로 시작하면 https: 붙이기
            if img_url.startswith("//"):
                img_url = "https:" + img_url

            # noimage 포함 시 무효 처리
            if "noimage" in img_url.lower():
                info["image_url"] = None
            else:
                info["image_url"] = img_url

        except Exception:
            info["image_url"] = None

        info["platform"] = Platform.GMARKET.value
        info["sale_link"] = self.driver.current_url
        return info

        return info
