from abc import ABC, abstractmethod

# detail parser 추상 클래스
# 사이트별로 해당 추상 클래스를 구현
class DetailParser(ABC):

    @abstractmethod
    def open_product_detail_page(self, url: str):
        """
        받아온 상품 상세페이지 url을 오픈
        :param url: 상품 상세 페이지 url
        :return:
        """
        pass

    @abstractmethod
    def get_product_info(self) -> dict:
        """
        상품명, 가격, 배송비, 브랜드 or 제조사, 이미지, 판매자 정보 크롤링
        :return:
        """
        pass

    def get_product_details(self, url: str) -> dict:
        """
        전체 플로우
        :return:
        """
        self.open_product_detail_page(url)
        return self.get_product_info()
