import re
import time
import logging
import undetected_chromedriver as uc

from typing import List
from selenium.common import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as ec
from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
from common.logging_utils import setup_logger
from crawler.review_parser import ReviewParser

# Logging 설정
setup_logger()

class AuctionReviewParser(ReviewParser):
    def __init__(self, max_pages: int = None):

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
        self.wait = WebDriverWait(self.driver, 10)

    # 제품 상세 페이지 열기
    def open_product_detail_page(self, url: str):
        logging.info(f"[open_product_detail_page] {url}")
        self.driver.get(url)
        time.sleep(2)

    # 리뷰 탭으로 이동
    def move_to_review(self):
        review_tab = self.wait.until(
            ec.presence_of_element_located((By.CSS_SELECTOR, "a[href*='#vip_tab_comment']"))
        )
        self.driver.execute_script("arguments[0].scrollIntoView(true);", review_tab)
        time.sleep(1)
        self.driver.execute_script("arguments[0].click();", review_tab)
        time.sleep(1)

        # 리뷰 없음 확인
        if self.driver.find_elements(By.CSS_SELECTOR, ".box__vip-review--none"):
            logging.info("[move_to_review] 리뷰 없음")
            self.no_review = True
        else:
            self.no_review = False

    # 리뷰 내용 파싱
    def get_review_info(self) -> List[dict]:
        # 리뷰 없는 경우
        if getattr(self, "no_review", False):
            return []

        # 리뷰 목록 로딩 대기
        self.wait.until(ec.presence_of_element_located((By.CSS_SELECTOR, "ul.list__review")))

        # 전체 리뷰 페이지 수 확인
        total_pages = self._get_total_pages()
        logging.info(f"[get_review_info] 총 리뷰 페이지 수: {total_pages}")

        # max_pages가 설정된 경우 max_pages => total_page
        if self.max_pages is not None:
            total_pages = min(total_pages, self.max_pages)

        all_reviews = []
        current_page = 1

        while current_page <= total_pages:
            logging.info(f"[get_review_info] {current_page} 페이지 처리 중...")

            if not self._move_page(current_page):
                logging.info("[get_review_info] 페이지 이동 실패. 종료")
                break

            # 리뷰 로딩
            self.wait.until(
                ec.presence_of_element_located((By.CSS_SELECTOR, "ul.list__review li.list-item"))
            )
            review_elements = self.driver.find_elements(By.CSS_SELECTOR,"ul.list__review > li.list-item")

            for r in review_elements:
                try:
                    all_reviews.append(self._parse_review(r))
                except Exception as e:
                    logging.exception("리뷰 파싱 오류:", e)

            current_page += 1

        logging.info(f"[get_review_info] 총 {len(all_reviews)}개 리뷰 수집 완료")
        return all_reviews

    # ------------------------ 내부 메서드 ------------------------
    # 전체 페이지 개수 파악
    def _get_total_pages(self) -> int:
        try:
            text = self.driver.find_element(By.CSS_SELECTOR,".box__page-jump span.text__total em.text").text
            return int(text)
        except NoSuchElementException:
            return 1

    # 특정 페이지로 이동
    def _move_page(self, page: int) -> bool:
        selector = f"a.link__page-number[data-page-index='{page}']"
        page_buttons = self.driver.find_elements(By.CSS_SELECTOR, selector)

        if not page_buttons:
            # 페이지 번호 없으면 "다음" 버튼으로 그룹 이동
            next_btn = self.driver.find_elements(By.CSS_SELECTOR, "a.link__page-next")
            if next_btn:
                logging.info("[get_review_info][_move_page] 다음 페이지 그룹 이동")
                self.driver.execute_script("arguments[0].scrollIntoView(true);", next_btn[0])
                self.driver.execute_script("arguments[0].click();", next_btn[0])
                time.sleep(1.5)
                return False  # 같은 page index로 다시 탐색
            else:
                return False

        # 페이지 버튼 클릭
        btn = page_buttons[0]
        self.driver.execute_script("arguments[0].scrollIntoView(true);", btn)
        self.driver.execute_script("arguments[0].click();", btn)
        time.sleep(1.3)

        return True

    # 개별 리뷰 파싱(평점, 내용, 이미지, 작성 날짜)
    def _parse_review(self, review) -> dict:

        # 초기값 설정
        author_name = ""
        rating = None
        content = ""
        image_urls = []
        created_at = ""

        # 작성자
        try:
            author_name = review.find_element(By.CSS_SELECTOR, "p.text__writer").text
        except NoSuchElementException:
            author_name = ""
        except Exception as e:
            logging.exception("리뷰 작성자 파싱 오류:", e)
            raise

        # 평점
        try:
            star_fill = review.find_element(By.CSS_SELECTOR, ".image__star-fill")
            style_value = star_fill.get_attribute("style")
            m = re.search(r"(\d+)", style_value)
            if m:
                percent = int(m.group(1))
                rating = percent // 20
            else:
                rating = None
        except NoSuchElementException:
            rating = None
        except Exception as e:
            logging.exception("리뷰 평점 파싱 오류:", e)
            raise

        # 내용
        try:
            content = review.find_element(By.CSS_SELECTOR, ".box__review-text p.text").text.strip()
        except NoSuchElementException:
            content = ""
        except Exception as e:
            logging.exception("리뷰 내용 파싱 오류:", e)
            raise

        # 이미지
        try:
            thumbnails = review.find_elements(By.CSS_SELECTOR, ".box__list-thumbnail ul.list li.list-item a.link")
            for t in thumbnails:
                style = t.get_attribute("style")
                # url 추출
                m = re.search(r'url\(["\']?(.*?)["\']?\)', style)
                if m:
                    image_urls.append(m.group(1))
        except Exception as e:
            logging.exception("리뷰 이미지 파싱 오류:", e)
            raise

        # 작성 날짜
        try:
            created_at = review.find_element(By.CSS_SELECTOR, "p.text__date").text
        except NoSuchElementException:
            created_at = ""
        except Exception as e:
            logging.exception("리뷰 작성 날짜 파싱 오류:", e)
            raise

        return {
            "author_name": author_name,
            "title": "",
            "rating": rating,
            "created_at": created_at,
            "content": content,
            "image_urls": image_urls,
        }

    def quit(self):
        self.driver.quit()
