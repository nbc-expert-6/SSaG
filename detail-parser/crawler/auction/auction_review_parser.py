import re
import time
import logging
import undetected_chromedriver as uc

from typing import List
from selenium.common import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as ec
from ...common.logging_utils import setup_logger
from ..review_parser import ReviewParser

# Logging 설정
setup_logger()

class AuctionReviewParser(ReviewParser):
    def __init__(self, max_pages: int = None):
        self.no_review = None
        self.max_pages = max_pages

        options = uc.ChromeOptions()
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        self.driver = uc.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, 10)

    def open_product_detail_page(self, url: str):
        logging.info(f"[open_product_detail_page] {url}")
        self.driver.get(url)
        time.sleep(2)

    def move_to_review(self):
        review_tab = self.wait.until(
            ec.presence_of_element_located((By.CSS_SELECTOR, "a[href*='#vip_tab_comment']"))
        )
        self.driver.execute_script("arguments[0].scrollIntoView(true);", review_tab)
        time.sleep(1)
        self.driver.execute_script("arguments[0].click();", review_tab)
        time.sleep(2)

        # 리뷰 없음 확인
        if self.driver.find_elements(By.CSS_SELECTOR, ".box__vip-review--none"):
            logging.info("[move_to_review] 리뷰 없음")
            self.no_review = True
        else:
            self.no_review = False

    def get_review_info(self) -> List[dict]:
        if getattr(self, "no_review", False):
            return []

        # 리뷰 목록 로딩 대기
        self.wait.until(ec.presence_of_element_located((By.CSS_SELECTOR, "ul.list__review")))

        total_pages = self._get_total_pages()
        logging.info(f"[get_review_info] 총 리뷰 페이지 수: {total_pages}")

        # max_pages가 설정한 경우 max_pages => total_page
        if self.max_pages is not None:
            total_pages = min(total_pages, self.max_pages)

        all_reviews = []
        current_page = 1

        while current_page <= total_pages:
            logging.info(f"[get_review_info] {current_page} 페이지 처리 중...")

            if not self._move_page(current_page):
                logging.info("[get_review_info] 페이지 이동 실패. 종료")
                break

            # 리뷰 로딩 대기
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

        print(f"\n[get_review_info] 총 {len(all_reviews)}개 리뷰 수집 완료")
        return all_reviews

    # ------------------------ 내부 메서드 ------------------------
    def _get_total_pages(self) -> int:
        """전체 페이지 개수 파악"""
        try:
            text = self.driver.find_element(
                By.CSS_SELECTOR,
                ".box__page-jump span.text__total em.text"
            ).text
            return int(text)
        except NoSuchElementException:
            return 1

    def _move_page(self, page: int) -> bool:
        """특정 페이지로 이동 (페이지 그룹 이동 포함)"""
        selector = f"a.link__page-number[data-page-index='{page}']"
        page_buttons = self.driver.find_elements(By.CSS_SELECTOR, selector)

        if not page_buttons:
            # 페이지 번호 없으면 "다음" 버튼으로 그룹 이동
            next_btn = self.driver.find_elements(By.CSS_SELECTOR, "a.link__page-next")
            if next_btn:
                print("➡ 다음 페이지 그룹 이동")
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

    def _parse_review(self, review) -> dict:
        """리뷰 하나 파싱"""

        # 평점
        rating = None
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
        images = []
        try:
            thumbnails = review.find_elements(
                By.CSS_SELECTOR,
                ".box__list-thumbnail ul.list li.list-item a.link"
            )
            for t in thumbnails:
                style = t.get_attribute("style")
                m = re.search(r'url\(["\']?(.*?)["\']?\)', style)
                if m:
                    images.append(m.group(1))
        except NoSuchElementException:
            images = []
        except Exception as e:
            logging.exception("리뷰 이미지 파싱 오류:", e)
            raise

        # 작성 날짜
        try:
            date = review.find_element(By.CSS_SELECTOR, "p.text__date").text
        except NoSuchElementException:
            date = ""
        except Exception as e:
            logging.exception("리뷰 작성 날짜 파싱 오류:", e)
            raise

        return {
            "rating": rating,
            "content": content,
            "images": images,
            "date": date
        }

    def quit(self):
        self.driver.quit()
