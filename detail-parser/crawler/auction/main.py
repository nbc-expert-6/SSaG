from .auction_detail_parser import AuctionDetailParser
from .auction_review_parser import AuctionReviewParser
from ...common.kafka_utils import create_consumer
from ...common.logging_utils import setup_logger
import logging

# Logging 설정
setup_logger()

def process_product(product_id: str, urls: list):
    """
    상품 상세(1개) → 리뷰(n개) 순서로 처리
    (임시) 최대 6개의 리뷰 페이지 파싱
    """
    detail_parser = AuctionDetailParser()
    review_parser = AuctionReviewParser(max_pages=6)

    for url in urls:
        # 1) 상세 정보 파싱
        detail_info = detail_parser.get_product_details(url)
        logging.info(f"[DETAIL] {product_id} | {detail_info}")

        # TODO: 상세 정보 Kafka Publish -> Product Service

        # 2) 리뷰 파싱
        reviews = review_parser.get_reviews(url)
        logging.info(f"[REVIEW] {product_id} | {len(reviews)} reviews")

        # TODO: 리뷰 정보 Kafka Publish -> Product Service

    detail_parser.quit()
    review_parser.quit()


if __name__ == "__main__":
    consumer = create_consumer(
        topic="auction_links",
        group_id="auction-group"
    )

    for msg in consumer:
        logging.info(f"[KAFKA] message: {msg.value}")

        product_id = msg.value["id"]
        urls = msg.value["urls"]

        process_product(product_id, urls)
