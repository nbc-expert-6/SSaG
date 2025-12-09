import logging

from common.kafka_utils import create_consumer, create_producer
from common.logging_utils import setup_logger
from common.platform import Platform
from crawler.auction.auction_review_parser import AuctionReviewParser

# Kafka Consumer 설정
consumer = create_consumer(
    topic='auction-product-urls',
    group_id='auction-review-group'
)

# Kafka Producer 설정
producer = create_producer()

# Logging 설정
setup_logger()

if __name__ == "__main__":
    # 임시로 6개 페이지 파싱 설정
    parser = AuctionReviewParser(max_pages=6)

    for url_info in consumer:
        # 받은 url 정보
        logging.info("[auction-product-urls]: {}".format(url_info.value))
        main_product_id = url_info.value['main_product_id']
        urls = url_info.value['urls']

        # 제품 리뷰 정보 파싱 실행
        for url in urls:
            product_reviews = {}
            reviews = parser.get_reviews(url)

            if len(reviews) < 1:
                logging.info(f"[리뷰 없음]: {url}")
                continue

            product_reviews["main_product_id"] = main_product_id
            product_reviews["platform"] = Platform.AUCTION.value
            product_reviews["reviews"] = reviews

            producer.send('product-reviews', product_reviews)
            logging.info(f"[publish] product-reviews: {product_reviews}")


    # # 테스트용
    # urls = ["https://itempage3.auction.co.kr/DetailView.aspx?itemno=F301578522",
    #         "https://itempage3.auction.co.kr/detailview.aspx?ItemNo=A564284718"]
    # for url in urls:
    #     product_reviews = {}
    #     reviews = parser.get_reviews(url)
    #     product_reviews["main_product_id"] = "main_product_id"
    #     product_reviews["platform"] = "auction"
    #     product_reviews["reviews"] = reviews
    #
    #     logging.info(f"[publish] product-reviews: {product_reviews}")

    parser.quit()
