import os
import time
import requests
import undetected_chromedriver as uc
from crawler.detail_parser import DetailParser
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait

class CoupangDetailParser(DetailParser):
    def __init__(self, image_save_dir="images"):
        """
        :param image_save_dir: 이미지 저장 경로
        """
        # 드라이버 옵션
        options = uc.ChromeOptions()
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        # 드라이버 생성
        self.driver = uc.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, 10)

        # 이미지 저장 디렉토리
        self.image_save_dir = image_save_dir
        os.makedirs(image_save_dir, exist_ok=True)

    def open_product_detail_page(self, url: str):
        print(f"[INFO] 상품 페이지 오픈: {url}")
        self.driver.get(url)
        time.sleep(3)

    def get_product_info(self) -> dict:
        info = {}

        # 브랜드
        try:
            brand_elem = self.driver.find_element(By.CSS_SELECTOR, "div.twc-text-sm.twc-text-blue-600")
            info["brand"] = brand_elem.text.strip()
            print(f"[INFO] 브랜드: {info['brand']}")
        except:
            info["brand"] = None
            print("[WARN] 브랜드 정보를 찾을 수 없음")

        # 상품명
        try:
            title_elem = self.driver.find_element(By.CSS_SELECTOR, "h1.product-title span")
            info["name"] = title_elem.text.strip()
            print(f"[INFO] 상품명: {info['name']}")
        except:
            info["name"] = None
            print("[WARN] 상품명을 찾을 수 없음")

        # 가격
        try:
            price_elem = self.driver.find_element(
                By.CSS_SELECTOR,
                "div.price-amount.final-price-amount"
            )
            raw_price = price_elem.text.strip()  # 예: "35,900원"

            # 숫자만 추출
            import re
            numeric_price = int(re.sub(r"[^0-9]", "", raw_price))

            info["price"] = numeric_price
            print(f"[INFO] 가격: {info['price']}")
        except Exception as e:
            info["price"] = None
            print(f"[WARN] 가격 정보를 찾을 수 없음: {e}")

        import re

        # 배송비
        try:
            # 무료배송
            free_shipping = self.driver.find_elements(
                By.CSS_SELECTOR,
                "span.extreme-prominence-shipping-fee-txt"
            )
            if free_shipping:
                info["delivery_fee"] = 0
                print(f"[INFO] 배송비: {info['delivery_fee']}")
            else:
                # 배송비 금액
                fee_elem = self.driver.find_elements(
                    By.CSS_SELECTOR,
                    "div.price-shipping-fee-info-container div.twc-ml-\\[3px\\]"
                )
                if fee_elem:
                    raw_fee = fee_elem[0].text.strip()  # e.g. "15,000원"
                    # 숫자만 추출
                    numeric_fee = int(re.sub(r"[^0-9]", "", raw_fee))
                    info["delivery_fee"] = numeric_fee
                    print(f"[INFO] 배송비: {info['delivery_fee']}")
                else:
                    info["delivery_fee"] = None
                    print("[WARN] 배송비 정보를 찾을 수 없음")
        except Exception as e:
            info["delivery_fee"] = None
            print(f"[WARN] 배송비 파싱 중 오류 발생: {e}")

        # 판매자 이름
        try:
            seller_elem = self.driver.find_element(
                By.CSS_SELECTOR,
                "div.twc-flex.twc-flex-row.twc-justify-start.twc-items-center.twc-flex-wrap a"
            )

            # 첫 번째 텍스트 노드만 가져오기
            seller_name = self.driver.execute_script(
                "return arguments[0].childNodes[0].textContent.trim();",
                seller_elem
            )

            info['seller'] = seller_name
            print(f"[INFO] 판매자: {info['seller']}")
        except Exception as e:
            info["seller"] = None
            print(f"[WARN] 판매자 정보를 찾을 수 없음: {e}")

        # 이미지
        try:
            img_elem = self.driver.find_element(By.CSS_SELECTOR, "div.twc-relative img")
            img_url = img_elem.get_attribute("src")
            info["image_url"] = img_url
            print(f"[INFO] 이미지 URL: {img_url}")

            # 이미지 다운로드
            if img_url.startswith("//"):
                img_url = "https:" + img_url
            img_name = img_url.split("/")[-1]
            img_path = os.path.join(self.image_save_dir, img_name)
            r = requests.get(img_url, stream=True)
            if r.status_code == 200:
                with open(img_path, "wb") as f:
                    for chunk in r.iter_content(1024):
                        f.write(chunk)
                info["image_file"] = img_path
                print(f"[INFO] 이미지 다운로드 완료: {img_path}")
            else:
                info["image_file"] = None
                print("[WARN] 이미지 다운로드 실패")
        except:
            info["image_url"] = None
            info["image_file"] = None
            print("[WARN] 이미지 정보를 찾을 수 없음")

        return info

    def get_product_details(self, url: str) -> dict:
        print("[INFO] 상세 정보 크롤링 시작")
        self.open_product_detail_page(url)
        info = self.get_product_info()
        print("[INFO] 상세 정보 크롤링 완료")
        return info