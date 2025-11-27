from abc import ABC, abstractmethod
from typing import List

# url parser 추상 클래스
# 사이트별로 해당 추상 클래스를 구현
class UrlParser(ABC):

    # 생성자 (구현체별로 대상 사이트 url, 파싱할 상품 상세페이지 링크의 최대 개수)
    def __init__(self, url, max_links):
        self.url = url
        self.max_links = max_links

    @abstractmethod
    def open_main_page(self):
        """
        크롤링할 사이트의 메인 페이지 오픈
        :return:
        """
        pass

    @abstractmethod
    def search(self, keyword: str):
        """
        검색창에 키워드를 입력 후 검색
        :param keyword: [브랜드] [상품명] [모델명]
        :return:
        """
        pass

    @abstractmethod
    def sort_by_low_price(self):
        """
        낮은 가격순으로 정렬
        :return:
        """
        pass

    @abstractmethod
    def remove_add(self):
        """
        광고 데이터가 섞여있으면 제외
        :return:
        """
        pass

    @abstractmethod
    def get_product_links(self) -> List[str]:
        """
        상품상세 링크를 파싱 후 리스트로 반환
        :return: List[str]
        """
        pass

    @abstractmethod
    def get_product_urls(self, keyword: str) -> List[str]:
        """
        전체 플로우
        :return: List[str]
        """
        self.open_main_page()
        self.search(keyword)
        self.sort_by_low_price()
        self.remove_add()
        links = self.get_product_links()
        return links[:self.max_links]
