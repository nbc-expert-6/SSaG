import time
import re
import undetected_chromedriver as uc
from selenium.webdriver import ActionChains
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from common.config import TARGET_AUCTION_URL


class MainProductParser():
    def __init__(self, url=TARGET_AUCTION_URL):

        self.url = url
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

        kwargs = {}

        kwargs["options"] = options
        kwargs["use_subprocess"] = True
        kwargs["version_main"] = None

        self.driver = uc.Chrome(**kwargs)
        self.wait = WebDriverWait(self.driver, 10)


    def open_main_page(self):
        self.driver.get(self.url)

    def parse_all_categories(self) -> dict:
        result = {}

        # 1) 브랜드 패션 전체 hover
        menu = self.wait.until(
            EC.presence_of_element_located(
                (By.CSS_SELECTOR, ".navi_dropdown .allmenu_cate10")
            )
        )
        ActionChains(self.driver).move_to_element(menu).perform()
        time.sleep(0.1)

        # 2) depth2 전부
        depth2_elements = self.driver.find_elements(
            By.CSS_SELECTOR,
            ".navi_dropdown .allmenu_depth2_li > a.allmenu_depth2"
        )
        actions = ActionChains(self.driver)

        for d2 in depth2_elements:
            try:
                depth2_name = d2.text.strip()
                if not depth2_name:
                    continue

                # depth2 hover -> 해당 depth3 노출
                actions.move_to_element(d2).perform()
                time.sleep(0.3)

                parent_li = d2.find_element(By.XPATH, "./..")

                depth3_items = parent_li.find_elements(
                    By.CSS_SELECTOR,
                    ".allmenu_depth3_group .allmenu_depth3"
                )

                # depth3 없으면 스킵
                if not depth3_items:
                    continue

                result[depth2_name] = {}

                for item in depth3_items:
                    text = item.text.strip()
                    href = item.get_attribute("href")
                    if text and href:
                        result[depth2_name][text] = href

            except:
                continue

        return result

    def parse_products(self, category_url: str, large:str, medium: str) -> list[dict]:
        items: list[dict] = []
        print(large)
        # TODO: 상의, 하의, 아우터, 시즌의류를 UUID로 변경
        category_large_map = {
            "브랜드 여성의류": "여성",
            "브랜드 남성의류": "남성",
            "브랜드 잡화": "잡화",
        }
        category_medium_map = {
            "티셔츠": "상의",
            "블라우스/셔츠": "상의",
            "니트/가디건/베스트": "상의",
            "니트/가디건": "상의",
            "드레스셔츠/남방": "상의",
            "원피스": "하의",
            "팬츠": "하의",
            "스커트": "하의",
            "자켓/베스트": "아우터",
            "코트": "아우터",
            "점퍼/코트": "아우터",
            "자켓/블레이져": "아우터",
            "야상/점퍼/패딩": "아우터",
            "수영복/트레이닝": "시즌의류",
            "남성정장": "정장"
        }
        category_uuid_map = {
            # 여성
            "여성 상의": "f3c8a7f2-0001-0000-0000-000000000001",
            "여성 하의": "f3c8a7f2-0002-0000-0000-000000000002",
            "여성 아우터": "f3c8a7f2-0003-0000-0000-000000000003",
            "여성 시즌의류": "f3c8a7f2-0004-0000-0000-000000000004",

            # 남성
            "남성 상의": "7e2b1a9d-0001-0000-0000-000000000001",
            "남성 하의": "7e2b1a9d-0002-0000-0000-000000000002",
            "남성 아우터": "7e2b1a9d-0003-0000-0000-000000000003",
            "남성 시즌의류": "7e2b1a9d-0004-0000-0000-000000000004",
            "남성 정장": "7e2b1a9d-0005-0000-0000-000000000005",  # 추가된 항목

            # 잡화
            "여성화": "9a1c4e7f-0001-0000-0000-000000000001",
            "남성화": "9a1c4e7f-0002-0000-0000-000000000002",
            "캐주얼화": "9a1c4e7f-0003-0000-0000-000000000003",
            "여성가방": "9a1c4e7f-0004-0000-0000-000000000004",
            "남성가방": "9a1c4e7f-0005-0000-0000-000000000005",
            "캐주얼가방": "9a1c4e7f-0006-0000-0000-000000000006",
            "여행가방": "9a1c4e7f-0007-0000-0000-000000000007",
            "지갑/벨트": "9a1c4e7f-0008-0000-0000-000000000008",
            "패션잡화": "9a1c4e7f-0009-0000-0000-000000000009"
        }

        category_large = category_large_map.get(large)
        category_final = ""
        if category_large == "잡화":
            category_final = medium
        else:
            category_medium = category_medium_map.get(medium)
            category_final = f"{category_large} {category_medium}"

        category_final = category_uuid_map.get(category_final)

        self.driver.get(category_url)
        time.sleep(0.3)

        page = 1  # 페이지 로그

        while True:
            print(f"페이지: {page}")

            # 스크롤 하면서 카드 파싱
            cards = self.driver.find_elements(
                By.CSS_SELECTOR,
                ".component--item_card.type--general"
            )

            last_y = 0
            for card in cards:

                # 브랜드
                try:
                    brand = card.find_element(By.CSS_SELECTOR, ".text--brand").text.strip()
                    # 개행 제거
                    brand = brand.split("\n")[0].strip()
                    # '공식' 제거
                    if "공식" in brand:
                        brand = brand.replace("공식", "").strip()
                except:
                    brand = None

                # 브랜드 없으면 skip
                if not brand:
                    continue

                # 상품명(전처리)
                try:
                    title_container = card.find_element(By.CSS_SELECTOR, ".text--itemcard_title.ellipsis")
                    title_elem = title_container.find_element(By.CSS_SELECTOR, ".text--title")
                    raw_name = title_elem.text.strip()
                except:
                    raw_name = None

                name = self._clean_name(raw_name)  # 아래 함수 참고

                # 이미지 URL
                try:
                    img_elem = card.find_element(By.CSS_SELECTOR, ".section--itemcard_img img")
                    image_url = img_elem.get_attribute("src")
                    if image_url and image_url.startswith("//"):
                        image_url = "https:" + image_url
                except:
                    image_url = None

                data = {
                    "category_medium_id": category_final,
                    "brand": brand,
                    "name": name,
                    "image_url": image_url,
                }
                items.append(data)

                # 여기서 즉시 로그
                print(data)

            # 다음 페이지
            try:
                next_btn = self.driver.find_element(
                    By.CSS_SELECTOR,
                    ".component--pagination a.link--next_page"
                )
            except:
                break

            next_href = next_btn.get_attribute("href")
            if not next_href:
                break

            self.driver.get(next_href)
            page += 1
            time.sleep(0.3)

        return items


    # 제거 규칙 함수 추가
    def _clean_name(self, text: str) -> str:
        if not text:
            return None

        # 불필요한 prefix 제거
        remove_prefixes = [
            "무료배송)", "무료반품+무료배송)"
        ]
        for p in remove_prefixes:
            if text.startswith(p):
                text = text[len(p):]

        # 1) 정상 괄호 제거 (문자열 안에 뭐가 있어도 통째로)
        text = re.sub(r"[\(\[\{][^\)\]\}]*[\)\]\}]", "", text)

        # 2) 닫는 괄호만 있는 경우 제거 (문자열 + 괄호)
        text = re.sub(r"\S+\)", "", text)

        # 앞뒤 공백 제거
        return text.strip()