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

setup_logger()

class ElevenStReviewParser(ReviewParser):
    def __init__(self):
        self.no_review = None

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
        try:
            review_tab = self.wait.until(ec.presence_of_element_located((By.ID, "tabMenuDetail2")))
            self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", review_tab)
            time.sleep(0.5)

            self.wait.until(ec.element_to_be_clickable((By.ID, "tabMenuDetail2")))
            review_tab.click()
            time.sleep(1.2)
            logging.info("[move_to_review] 리뷰 탭 클릭 완료")
        except Exception as e:
            logging.error(f"[move_to_review] 실패: {e}")
            self.no_review = True

    def get_review_info(self) -> List[dict]:
        # 전체 리뷰 개수 파악
        try:
            num_el = self.driver.find_element(By.CSS_SELECTOR, "#prdReview .text_num")
            total_count = int(num_el.text.strip())
            logging.info(f"[get_review_info] 전체 리뷰 {total_count}개")
        except:
            total_count = 0

        if total_count == 0:
            logging.info("[get_review_info] 리뷰 없음")
            return []

        # iframe 전환
        try:
            self.wait.until(ec.frame_to_be_available_and_switch_to_it((By.ID, "ifrmReview")))
            logging.info("[get_review_info] iframe 전환 완료")
        except Exception as e:
            logging.exception(f"[get_review_info] iframe 실패: {e}")
            return []

        results = []
        loaded_count = 0

        while True:
            # 리뷰 요소 로딩
            try:
                self.wait.until(ec.presence_of_element_located((By.CSS_SELECTOR, "li.review_list_element")))
            except Exception as e:
                logging.exception("리뷰 요소 로딩 실패:", e)
                break

            review_items = self.driver.find_elements(By.CSS_SELECTOR, "li.review_list_element")

            for item in review_items[loaded_count:]:
                # 작성자
                try:
                    author_name = item.find_element(By.CSS_SELECTOR, ".c_product_reviewer .name").text.strip()
                except Exception as e:
                    logging.exception("리뷰 작성자 파싱 오류:", e)
                    author_name = ""

                # 평점
                try:
                    rating = int(item.find_element(By.CSS_SELECTOR, ".grade em").text)
                except Exception as e:
                    logging.exception("리뷰 평점 파싱 오류:", e)
                    rating = None

                # 내용
                try:
                    content = item.find_element(By.CSS_SELECTOR, ".cont_text_wrap p").get_attribute("innerText").strip()
                except NoSuchElementException:
                    content = ""
                except Exception as e:
                    logging.exception("리뷰 내용 파싱 오류:", e)
                    content = ""

                # 작성 날짜
                try:
                    created_at = item.find_element(By.CSS_SELECTOR, ".side .date").text.strip()
                except Exception as e:
                    logging.exception("리뷰 작성 날짜 파싱 오류:", e)
                    created_at = ""

                # 이미지
                image_urls = []
                try:
                    thumbs = item.find_elements(By.CSS_SELECTOR, ".c_product_review_thumbnail2 ul.list li button")
                    for btn in thumbs:
                        li = btn.find_element(By.XPATH, "./..")
                        if "item_video" in li.get_attribute("class"):
                            continue
                        style = btn.get_attribute("style")
                        m = re.search(r"url\(['\"]?(.*?)['\"]?\)", style)
                        if m:
                            image_urls.append(m.group(1))
                except Exception as e:
                    logging.exception("리뷰 이미지 파싱 오류:", e)

                results.append({
                    "author_name": author_name,
                    "title": "",
                    "rating": rating,
                    "created_at": created_at,
                    "content": content,
                    "image_urls": image_urls
                })

            loaded_count = len(results)
            if loaded_count >= total_count:
                break

            # 리뷰 더보기
            try:
                more_review_btn = self.driver.find_element(By.CSS_SELECTOR, ".review-next-list-div button")
            except NoSuchElementException:
                logging.info("리뷰 더보기 버튼 없음")
                break
            except Exception as e:
                logging.exception("리뷰 더보기 버튼 찾기 실패:", e)
                break

            try:
                self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", more_review_btn)
                time.sleep(0.3)
                more_review_btn.click()
                time.sleep(1.0)
            except Exception as e:
                logging.exception("리뷰 더보기 버튼 클릭 실패:", e)
                break

        # 메인 프레임으로 이동
        self.driver.switch_to.default_content()
        return results

    def quit(self):
        self.driver.quit()
