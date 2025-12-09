import os
import time
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from common.config import CHROME_BINARY, CHROMEDRIVER_PATH
import common.platform
from crawler.review_parser import ReviewParser


"""
쿠팡 리뷰 크롤러
제품 상세 링크 전달 시 자동으로 페이지 끝까지 리뷰 파싱
list[dict] 형태로 리뷰 목록을 응답
"""
class CoupangReviewParser(ReviewParser):

    # 생성자
    # 브라우저 옵션 설정 및 이미지 저장 경로 세팅
    def __init__(self, image_save_dir="review_images"):

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

    """
    상품상세 페이지를 받아 브라우저로 오픈
    """
    def open_product_detail_page(self, url: str):
        self.driver.get(url)
        time.sleep(3)

    """
    상세정보 페이지의 리뷰 탭으로 이동 (요소 클릭 기반)
    리뷰가 0개면 탐색 종료
    """
    def move_to_review(self):
        try:
            review_tab = self.driver.find_element(By.XPATH, "//a[contains(text(),'상품평')]")
            review_count_text = review_tab.text
            count = int(''.join(filter(str.isdigit, review_count_text)))
            print(f"[INFO] 리뷰 개수: {count}")
            if count == 0:
                print("[INFO] 리뷰 없음")
                self.has_review = False
                return
            self.has_review = True
            review_tab.click()
            time.sleep(2)
        except Exception as e:
            self.has_review = False
            print(f"[WARN] 리뷰 탭을 찾을 수 없음: {e}")

    """
    현재 리뷰 페이징블록 정보를 로드 (시작 페이지, 끝 페이지, 햔재 페이지)
    현재 페이지가 페이징블록(10단위) 끝에 도달할 때 까지 다음 페이지 버튼 클릭
    """
    def click_next_page(self):
        try:
            paging_div = self.driver.find_element(By.XPATH, "//div[@data-page][@data-start][@data-end]")
            current_page = int(paging_div.get_attribute("data-page"))
            end_page = int(paging_div.get_attribute("data-end"))

            if current_page >= end_page:
                return False

            next_page = current_page + 1
            next_btn = self.driver.find_element(By.XPATH, f"//button[.//span[text()='{next_page}']]")
            next_btn.click()
            time.sleep(1)
            print(f"[INFO] 다음 페이지 이동: {next_page}")
            return True

        except Exception as e:
            print(f"[info] 다음 페이지 없음")
            return False

    """
    현재 페이지가 블록의 끝에 도달하면 다음 페이징 블록으로 이동
    그냥 클릭하면 현재 페이지 + 10으로 동작하기 때문에 클릭 전에 현재 페이지의 첫 페이지로 이동 후 클릭
    """
    def click_next_block(self):
        try:
            # 페이징 블록 가져오기
            pagination = WebDriverWait(self.driver, 5).until(
                EC.presence_of_element_located((By.XPATH, "//div[@data-start][@data-end]"))
            )

            # 다음 블록 버튼 찾기 (SVG 있는 버튼)
            # 전체 버튼 가져와서 끝에서 첫번째로 탐색
            buttons = pagination.find_elements(By.XPATH, ".//button")
            next_block_btn = None
            for btn in buttons[::-1]:  # 오른쪽 끝부터 탐색
                if btn.find_elements(By.TAG_NAME, "svg"):
                    next_block_btn = btn
                    break

            if next_block_btn.get_attribute("disabled") is not None:
                print("[INFO] 다음 블록 없음")
                return False

            # 다음 페이지 버튼을 클릭할 수 있을 경우 진입
            # 현재 페이지의 첫 페이지 클릭
            start_page = int(pagination.get_attribute("data-start"))
            first_page_btn = pagination.find_element(By.XPATH, f".//button[.//span[text()='{start_page}']]")
            self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", first_page_btn)
            first_page_btn.click()
            time.sleep(0.5)
            print(f"[INFO] 현재 블록 첫 페이지 {start_page} 클릭 완료")

            # 다시 페이징 블록으로 스크롤
            self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", pagination)
            time.sleep(0.2)

            # 다음 블록 버튼 클릭
            self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", next_block_btn)
            next_block_btn.click()
            time.sleep(2)
            print("[INFO] 다음 블록(>) 클릭 완료")

            return True

        except Exception as e:
            print(f"[info] 다음 블록 없음")
            return False

    """
    리뷰에서 제목, 내용, 생성일, 평점, 이미지를 파싱
    현재 페이지에 존재하는 모든 리뷰를 파싱
    페이지가 바뀔 때마다 동작
    """
    def get_review_info(self):
        if not getattr(self, "has_review", False):
            return []

        # 현재 페이지 번호 가져오기
        try:
            paging_div = self.driver.find_element(By.XPATH, "//div[@data-page][@data-start][@data-end]")
            current_page = int(paging_div.get_attribute("data-page"))
        except:
            current_page = 1

        reviews = []
        articles = self.driver.find_elements(By.XPATH, "//article[contains(@class, 'twc-border-b')]")
        print(f"[INFO] 발견된 리뷰 수: {len(articles)}")

        for idx, article in enumerate(articles, 1):
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", article)

            review_data = {}
            review_data["platform"] = common.platform.Platform.COUPANG.value

            # 제목 파싱
            try:
                review_data["title"] = article.find_element(
                    By.XPATH, ".//div[contains(@class,'twc-font-bold') and contains(@class,'twc-text-bluegray-900')]"
                ).text.strip()
            except:
                review_data["title"] = None

            # 작성자명 파싱
            try:
                author_elem = article.find_element(
                    By.XPATH,
                    ".//div[contains(@class,'twc-flex twc-items-center')]//span[@data-member-id]"
                )
                review_data["author_name"] = author_elem.text.strip()
            except:
                review_data["author_name"] = None

            # 내용 파싱
            try:
                review_data["content"] = article.find_element(
                    By.XPATH, ".//div[contains(@class,'twc-break-all')]//span"
                ).text.strip()
            except:
                review_data["content"] = None

            # 생성일 파싱
            try:
                review_data["created_at"] = article.find_element(
                    By.XPATH,
                    ".//div[contains(@class,'twc-items-center')]//div[contains(@class,'twc-text-bluegray-700')]"
                ).text.strip()
            except:
                review_data["created_at"] = None

            # 평점 파싱
            try:
                star_elems = article.find_elements(
                    By.XPATH, ".//i[contains(@class,'twc-bg-full-star') or contains(@class,'twc-bg-empty-star')]"
                )
                review_data["rating"] = sum(1 for s in star_elems if "full-star" in s.get_attribute("class"))
            except:
                review_data["rating"] = None

            # 이미지 파싱
            try:
                img_elems = article.find_elements(By.XPATH, ".//div[contains(@class,'twc-relative')]//img")
                img_urls = []
                for img in img_elems:
                    img_url = img.get_attribute("src")
                    if img_url and img_url.startswith("//"):
                        img_url = "https:" + img_url
                    if img_url:
                        img_urls.append(img_url)
                review_data["image_urls"] = img_urls
            except:
                review_data["image_urls"] = []

            reviews.append(review_data)

        return reviews


    """
    전체 페이지를 순회하며 해당 페이지에 있는 리뷰를 전부 파싱
    1차 유즈케이스 조합
    """
    def crawl_all_review_pages(self):
        all_reviews = []

        while True:
            reviews = self.get_review_info()
            all_reviews.extend(reviews)


            if self.click_next_page():
                continue

            if self.click_next_block():
                self.click_next_page()
                continue

            break

        print(f"[INFO] 전체 리뷰 총 {len(all_reviews)}개")
        return all_reviews

    """
    전체 유즈케이스 조합
    """
    def get_reviews(self, link):
        print(f"[INFO] 리뷰 수집 시작: {link}")

        self.open_product_detail_page(link)
        self.move_to_review()

        # 리뷰 없으면 바로 종료
        if not getattr(self, "has_review", False):
            print("[INFO] 리뷰가 없어 수집을 종료합니다.")
            return []

        all_reviews = self.crawl_all_review_pages()
        print(f"[INFO] 총 리뷰 수집 완료: {len(all_reviews)}개")
        return all_reviews