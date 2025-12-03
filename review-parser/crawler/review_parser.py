from abc import ABC, abstractmethod
from typing import List


# review parser 추상 클래스
# 사이트별로 해당 추상 클래스를 구현
class ReviewParser(ABC):

    # 리뷰 이미지, 리뷰 평점, 리뷰 제목, 리뷰 내용
    @abstractmethod
    def open_product_detail_page(self, url: str):
        """
        받아온 상품 상세페이지 url을 오픈
        :param url: 상품 상세 페이지 url
        :return:
        """
        pass

    # 페이징 처리된 리뷰 탐색

    # 이미지 리스트
    # 리뷰 (글 내용에 이미지 포함)
    @abstractmethod
    def move_to_review(self):
        """
        리뷰 탭으로 이동
        :return:
        """
        pass


    @abstractmethod
    def get_review_info(self) -> List[dict]:
        """
        페이지 정보 파악 (전체 몇개, 페이지 몇개, 페이지당 데이터 몇개)
        페이지를 순회하면서 데이터 저장
        반환
        :return:
        """
        pass

    def get_reviews(self,url: str) -> List[dict]:
        self.open_product_detail_page(url)
        self.move_to_review()
        reviews = self.get_review_info()
        return reviews
