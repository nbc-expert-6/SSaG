from common.kafka_utils import create_consumer
from common.logging_utils import setup_logger
import logging
from crawler.gmarket.gmarket_detail_parser import GmarketDetailParser
from crawler.gmarket.gmarket_review_parser import GmarketReviewParser


setup_logger()

def process_product(product_id: str, urls: list):
    detail_parser = GmarketDetailParser()
    review_parser = GmarketReviewParser()

    for url in urls:
        detail_info = detail_parser.get_product_details(url)
        logging.info(f"[DETAIL] {product_id} | {detail_info}")

        reviews = review_parser.get_reviews(url)
        logging.info(f"[REVIEW] {product_id} | {len(reviews)} reviews")

    detail_parser.quit()
    review_parser.quit()

if __name__ == "__main__":
    consumer = create_consumer(
        topic="gmarket-product-urls",
        group_id="gmarket-group"
    )

    for msg in consumer:
        logging.info(f"[KAFKA] message: {msg.value}")

        product_id = msg.value["main_product_id"]
        urls = msg.value["urls"]

        process_product(product_id, urls)