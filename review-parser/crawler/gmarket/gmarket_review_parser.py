import hashlib
import re
import time
from typing import List, Optional

import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
from common.platform import Platform

"""
지마켓 리뷰 크롤러
상품평이 없으면 종료
프리미엄 상품평 먼저, 일반 상품평은 그 이후 파싱
페이지 버튼이 아닌 드롭다운 메뉴로 페이지를 1씩 증가시켜가며 파싱
"""
class GmarketReviewParser:

    """
    브라우저 세팅
    """
    def __init__(self, image_save_dir: str = "review_images"):

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

    def open_product_detail_page(self, url: str):
        self.driver.get(url)
        time.sleep(1)

    """
    프리미엄 상품평이 비어있는지 확인 (비어있으면 스킵)
    """
    def _has_premium_reviews(self) -> bool:
        try:
            empty = self.driver.find_elements(
                By.CSS_SELECTOR,
                "table.tb_comment.tb_premium tbody tr.board_empty"
            )
            return len(empty) == 0
        except:
            return True
    """
    일반 상품평이 비어있는지 확인 (비어있으면 스킵)
    """
    def _has_normal_reviews(self) -> bool:
        try:
            empty = self.driver.find_elements(
                By.CSS_SELECTOR,
                "div.board_comment table.tb_comment_common tbody tr.board_empty"
            )
            return len(empty) == 0
        except:
            return True

    """
    리뷰 탭으로 이동, 이동 전에 리뷰 숫자 확인 후 0이면 파싱 종료
    """
    def move_to_review(self) -> bool:
        try:
            # 리뷰 개수 먼저 확인
            review_count_elem = self.driver.find_element(By.ID, "txtReviewTotalCount")
            text = review_count_elem.text.strip()

            # 숫자만 남기기 (,제거)
            clean = text.replace(",", "")

            review_count = int(clean)
            if review_count == 0:
                print("[INFO] 리뷰가 존재하지 않음")
                return False

            tab = self.wait.until(EC.element_to_be_clickable(
                (By.CSS_SELECTOR, "li.uxetabs_menu a[href='#vip-tab_comment']")
            ))
            self.driver.execute_script(
                "arguments[0].scrollIntoView({behavior:'auto', block:'center'});", tab
            )
            tab.click()
            time.sleep(1)
            return True

        except Exception as e:
            print("[WARN] 리뷰 탭 이동 실패:", e)
            return False

    """
    프리미엄 상품평과 일반 상품평의 드롭다운 박스를 구별할 수 있는 셀렉터를 인자로 넘겨줌
    드롭다운 박스에는 전체 페이지가 li로 들어있는데 이 숫자를 세서 전체 페이지 정보를 넘겨주는 유틸 함수
    """
    def _get_page_numbers_from_dropdown(self, dropdown_btn_selector: str) -> List[int]:
        # 리뷰 페이지 드롭다운 버튼 클릭 후 페이지 번호 리스트 추출
        try:
            btns = self.driver.find_elements(By.CSS_SELECTOR, dropdown_btn_selector)
            if not btns:
                # 드롭다운 없으면 빈 리스트 반환 → 2페이지가 없다는 뜻 (1페이지만 파싱)
                return []

            btn = btns[0]
            btn.click()
            time.sleep(0.3)

            dropdown_list = btn.find_element(By.XPATH, "following-sibling::ul")
            dropdown_links = dropdown_list.find_elements(By.CSS_SELECTOR, "li a[data-page-no]")

            pages = []
            for link in dropdown_links:
                val = link.get_attribute("data-page-no")
                if val and val.isdigit():
                    pages.append(int(val))
            pages = sorted(set(pages))
            btn.click()
            return pages
        except Exception as e:
            print("[WARN] 페이지 목록 찾기 실패:", e)
            return []

    """
    지마켓은 개별 리뷰의 평점은 없고 전체 평점만 계산해서 표시
    개별 리뷰의 평점을 전체 평점으로 세팅하기 위해 전체 평점 파싱
    """
    def _get_product_rating(self) -> Optional[float]:
        # 상품 전체 평점 추출
        try:
            rating_elem = self.driver.find_element(
                By.CSS_SELECTOR, "div.box__score-awards span.text__score"
            )
            # 줄바꿈 제거 후 숫자만 추출
            # '평점\n4.6' -> ['평점', '4.6'] -> '4.6'
            score_text = rating_elem.text.strip().split()[-1]
            rating = float(score_text)
            print(f"[INFO] 상품 전체 평점: {rating}")
            return rating
        except Exception as e:
            print(f"[WARN] 평점 추출 실패: {e}")
            return None

    """
    현재 페이지의 프리미엄 상품평을 파싱
    """
    def _parse_premium_table(self, rating: Optional[float] = None) -> List[dict]:
        results = []
        rows = self.driver.find_elements(By.CSS_SELECTOR, "table.tb_comment.tb_premium tbody tr")
        for row in rows:
            try:
                # 리뷰 아이디
                review_id = None
                try:
                    link = row.find_element(By.CSS_SELECTOR, "a.uxelayer_ctrl")
                    pop_url = link.get_attribute("data-pop-layer-url")
                    m = re.search(r"prvw_no=(\d+)", pop_url)
                    if m:
                        review_id = m.group(1)
                except:
                    pass

                # 작성자
                author_name = ""
                try:
                    author_name = row.find_element(By.CSS_SELECTOR,
                                                   "td.info dl.writer-info dd:nth-of-type(1)").text.strip()
                except:
                    pass

                # 등록일
                created_at = row.find_element(By.CSS_SELECTOR, "td.info dl.writer-info dd:nth-of-type(2)").text.strip()

                # 제목
                title = ""
                try:
                    title = row.find_element(By.CSS_SELECTOR, "p.comment-tit").text.strip()
                except:
                    pass

                # 내용
                content = ""
                try:
                    content = row.find_element(By.CSS_SELECTOR, "p.con").text.strip()
                except:
                    pass

                # 이미지
                img_el = row.find_elements(By.CSS_SELECTOR, "td.thumb img")
                image_url: Optional[str] = None
                if img_el:
                    src = img_el[0].get_attribute("src")
                    if src and src.startswith("//"):
                        src = "https:" + src
                    image_url = src

                review = {
                    "id": review_id,
                    "author_name": author_name,
                    "created_at": created_at,
                    "image_urls": image_url,
                    "title": title,
                    "content": content,
                    "rating": rating
                }
                print(review)
                results.append(review)
            except Exception as e:
                print(f"[WARN] Premium 리뷰 행 파싱 실패: {e}")
                continue
        return results

    """
    현재 페이지의 일반 상품평을 파싱
    """
    def _parse_normal_table(self, rating: Optional[float] = None) -> List[dict]:
        results = []
        rows = self.driver.find_elements(By.CSS_SELECTOR, "table.tb_comment.tb_comment_common tbody tr")
        for row in rows:
            try:
                author_name = ""
                try:
                    author_name = row.find_element(By.CSS_SELECTOR,
                                                   "td.info dl.writer-info dd:nth-of-type(1)").text.strip()
                except:
                    pass

                created_at = row.find_element(By.CSS_SELECTOR, "td.info dl.writer-info dd:nth-of-type(2)").text.strip()

                title = ""
                try:
                    title = row.find_element(By.CSS_SELECTOR, "p.comment-tit").text.strip()
                except:
                    try:
                        title = row.find_element(By.CSS_SELECTOR, "p.pd-tit").text.strip()
                    except:
                        pass

                content = ""
                try:
                    content = row.find_element(By.CSS_SELECTOR, "p.con").text.strip()
                except:
                    pass

                # 리뷰 ID (일반: 파생 키)
                review_id = self._make_review_key(author_name, created_at, content)

                review = {
                    "id": review_id,
                    "author_name": author_name,
                    "created_at": created_at,
                    "image_urls": None,
                    "title": title,
                    "content": content,
                    "rating": rating,
                }
                results.append(review)
                print(review)
            except Exception as e:
                print(f"[WARN] 일반 리뷰 행 파싱 실패: {e}")
                continue
        return results

    def _make_review_key(self, author: str, created_at: str, content: str) -> str:
        raw = f"{author}|{created_at}|{content}"
        return hashlib.sha1(raw.encode("utf-8")).hexdigest()[:16]

    """
    프리미엄 상품평 파싱 유즈케이스
    페이지를 순회하면서 해당 페이지의 상품평을 파싱
    """
    def crawl_premium_reviews(self, rating: Optional[float] = None) -> List[dict]:
        # 프리미엄 테이블 비어있으면 바로 종료
        if not self._has_premium_reviews():
            print("[INFO] 프리미엄 리뷰가 존재하지 않음")
            return []

        results = []
        DROPDOWN_BTN = "div#vip-tab_comment div.board_paging button.uxeselect_btn"
        pages = self._get_page_numbers_from_dropdown(DROPDOWN_BTN)

        if not pages:
            return self._parse_premium_table(rating=rating)

        # 1페이지는 UI가 이미 선택되어 있어 클릭 안 됨 → 직접 파싱만 수행
        print("[INFO] Premium 1페이지 파싱")
        results.extend(self._parse_premium_table(rating=rating))

        for page in pages:
            if page == 1:
                continue  # 1페이지는 스킵

            try:
                print(f"[INFO] Premium 리뷰 페이지 이동: {page}")

                btn = self.wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, DROPDOWN_BTN)))
                btn.click()
                time.sleep(0.2)

                page_link = self.wait.until(EC.element_to_be_clickable(
                    (By.CSS_SELECTOR, f"ul.select-commont-list li a[data-page-no='{page}']")
                ))
                page_link.click()
                time.sleep(1)

                results.extend(self._parse_premium_table(rating=rating))

            except Exception as e:
                print(f"[WARN] Premium 페이지 이동/파싱 실패: {e}")

        return results

    """
    일반 상품평 파싱 유즈케이스
    페이지를 순회하면서 해당 페이지의 상품평을 파싱
    """
    def crawl_normal_reviews(self, rating: Optional[float] = None) -> List[dict]:
        if not self._has_normal_reviews():
            print("[INFO] 일반 리뷰가 존재하지 않음")
            return []
        results = []
        DROPDOWN_BTN = "div#text-pagenation-wrap button.uxeselect_btn"

        pages = self._get_page_numbers_from_dropdown(DROPDOWN_BTN)
        print(f"[INFO] 일반 리뷰 총 페이지: {pages}")

        if not pages:
            return self._parse_normal_table(rating=rating)

        print("[INFO] 일반 리뷰 1페이지 파싱")
        results.extend(self._parse_normal_table(rating=rating))

        for page in pages:
            if page == 1:
                continue

            try:
                print(f"[INFO] 일반 리뷰 페이지 이동: {page}")

                btn = self.wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, DROPDOWN_BTN)))

                self.driver.execute_script(
                    "arguments[0].scrollIntoView({behavior:'instant', block:'center'});",
                    btn
                )
                time.sleep(0.15)

                btn.click()
                time.sleep(0.2)

                dropdown_ul = btn.find_element(By.XPATH, "following-sibling::ul")

                self.driver.execute_script(
                    "arguments[0].scrollIntoView({behavior:'instant', block:'center'});",
                    dropdown_ul
                )
                time.sleep(0.1)

                page_link = dropdown_ul.find_element(
                    By.XPATH, f".//a[@data-page-no='{page}']"
                )
                page_link.click()

                time.sleep(1)
                results.extend(self._parse_normal_table(rating=rating))
            except Exception as e:
                print(f"[WARN] 일반 페이지 이동/파싱 실패: {e}")
                continue

        return results

    """
    전체 유즈케이스
    외부 호출용
    프리미엄 상품평과 일반 상품평을 따로 파싱후 하나로 합쳐 응답
    """
    def get_reviews(self, url: str) -> List[dict]:
        self.open_product_detail_page(url)
        if not self.move_to_review():
            return []

        time.sleep(1)
        product_rating = self._get_product_rating()
        premium = self.crawl_premium_reviews(rating=product_rating)
        normal = self.crawl_normal_reviews(rating=product_rating)
        return premium + normal

    def quit(self):
        self.driver.quit()