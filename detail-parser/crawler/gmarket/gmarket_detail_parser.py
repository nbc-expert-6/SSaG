import time
import re

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
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

        # 브라우저 안정화 시간 체크
        # 타이틀에 접근 가능하면 브라우저 준비됨
        for _ in range(10):
            try:
                _ = self.driver.title
                break
            except:
                time.sleep(0.3)

        self.wait = WebDriverWait(self.driver, 10)

    """
    드라이버 크래시(NoSuchWindow 등) 발생 시
    자동으로 드라이버를 재생성하여 1회 재시도
    """
    def open_product_detail_page(self, url: str):
        for attempt in range(2):  # 최대 2번까지
            try:
                # 브라우저 창이 살아있는지 체크
                _ = self.driver.title
                self.driver.get(url)
                time.sleep(1)
                return

            except Exception:
                # 드라이버 죽었으면 종료
                try:
                    self.driver.quit()
                except:
                    pass

                # 마지막 시도가 아니면 재생성 후 재시도
                if attempt == 0:
                    self.__init__()
                    continue

                # 마지막 시도 실패 → 예외 그대로 throw
                raise

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

        price_text = None

        if coupon_price:
            price_text = coupon_price[0].text.strip()
        else:
            normal_price = self.driver.find_elements(
                By.CSS_SELECTOR, ".price_innerwrap:not(.price_innerwrap-coupon) strong.price_real"
            )
            if normal_price:
                price_text = normal_price[0].text.strip()

        if price_text:
            # 숫자만 추출
            nums = re.findall(r"\d+", price_text.replace(",", ""))
            if nums:
                info["price"] = int("".join(nums))
            else:
                # SOLD OUT / 일시품절 / 재고없음 등 숫자가 없으면 무효 처리
                info["price"] = None
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

        info["sale_link"] = self.driver.current_url
        return info

    def quit(self):
        self.driver.quit()
