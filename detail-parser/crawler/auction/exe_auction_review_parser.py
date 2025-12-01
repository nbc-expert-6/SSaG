from .auction_review_parser import AuctionReviewParser
from ...common.logging_utils import setup_logger
from ...common.kafka_utils import create_consumer
import logging

# Kafka Consumer 설정
consumer = create_consumer(
    topic='auction_links',
    group_id='auction-review-group'
)

# Logging 설정
setup_logger()

if __name__ == "__main__":
    # 임시로 6개 페이지 파싱 설정
    parser = AuctionReviewParser(max_pages=6)

    for url_info in consumer:
        # url 정보
        logging.info("url_info: {}".format(url_info.value))
        product_id = url_info.value['id']
        urls = url_info.value['urls']

        # 대표 상품에 대한 리뷰 수집
        logging.info(f"product_id: {product_id}")
        for url in urls:
            reviews = parser.get_reviews(url)
            for r in reviews:
                logging.info(f"review: {r}")

    # TODO: kafka publish( -> product-service에서 consume)

    # # 동작 테스트용 귤 판매 페이지(리뷰 페이지 수 6768개)
    # url = "https://itempage3.auction.co.kr/detailview.aspx?ItemNo=A564284718"
    # reviews = parser.get_reviews(url)
    #
    # for r in reviews:
    #     logging.info(f"review: {r}")

    parser.quit()
