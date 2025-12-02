import time

from crawler.gmarket.gmarket_review_parser import GmarketReviewParser

# todo: 컨슈머에서 링크 및 대표상품 정보 소비 추가

# 리뷰 많은 상품
link = 'https://item.gmarket.co.kr/Item?spm=gmktpc.searchlist.toporganictier.d0_3.5397269fOYbasO&goodscode=3244694250&utparam-url=%7B%22scene%22%3A%22search%22%2C%22sub_scene%22%3A%22main_srp%22%2C%22x_object_id%22%3A%223244694250%22%2C%22x_object_type%22%3A%22item%22%2C%22x_sku_id%22%3A%220%22%2C%22pageIndex%22%3A%220%22%2C%22pagePos%22%3A%223%22%2C%22pageSize%22%3A%224%22%2C%22listno%22%3A%223%22%2C%22sort%22%3A%22popularity%22%2C%22searchScenario%22%3A%22keyword%22%2C%22query%22%3A%22%EB%82%98%EC%9D%B4%ED%82%A4%20%EC%97%90%EC%96%B4%ED%8F%AC%EC%8A%A4%22%2C%22pvid%22%3A%22c70255b47c2b425bbd4e417cec5c9b75%22%2C%22pvid_sys%22%3A%22gmarket%20server%22%2C%22search_session_id%22%3A%22a223309e-cf91-403c-a86c-bc32a5a3c45f%22%2C%22origin_price%22%3A%2267700%22%2C%22promotion_price%22%3A%2267700%22%2C%22coupon_price%22%3A%2264320%22%2C%22ab_buckets%22%3A%22%2399%5E2%23C%22%2C%22trafficType%22%3A%22organic%22%7D'

# 리뷰 없는 상품
# link = 'https://item.gmarket.co.kr/Item?spm=gmktpc.searchlist.organictier.d0_58.8dc77f35E5j79Y&goodscode=4250796043&utparam-url=%7B%22scene%22%3A%22search%22%2C%22sub_scene%22%3A%22main_srp%22%2C%22x_object_id%22%3A%224250796043%22%2C%22x_object_type%22%3A%22item%22%2C%22x_sku_id%22%3A%220%22%2C%22pageIndex%22%3A%220%22%2C%22pagePos%22%3A%2258%22%2C%22pageSize%22%3A%2260%22%2C%22listno%22%3A%2258%22%2C%22sort%22%3A%22priceasc%22%2C%22searchScenario%22%3A%22keyword%22%2C%22query%22%3A%22%EB%82%98%EC%9D%B4%ED%82%A4%20%EC%97%90%EC%96%B4%ED%8F%AC%EC%8A%A4%20cw2288-111%22%2C%22pvid%22%3A%22fcfb553ceb9b49d7942555b096237408%22%2C%22pvid_sys%22%3A%22gmarket%20server%22%2C%22search_session_id%22%3A%2224980bab-e87b-4841-b5df-f941c70fb851%22%2C%22origin_price%22%3A%22139000%22%2C%22promotion_price%22%3A%22%22%2C%22coupon_price%22%3A%22129000%22%2C%22ab_buckets%22%3A%22%22%2C%22trafficType%22%3A%22organic%22%7D'

# 일반 상품평만 하나 있는 상품
# link = 'https://item.gmarket.co.kr/Item?spm=gmktpc.searchlist.organictier.d0_9.8dc77f35E5j79Y&goodscode=4370147410&utparam-url=%7B%22scene%22%3A%22search%22%2C%22sub_scene%22%3A%22main_srp%22%2C%22x_object_id%22%3A%224370147410%22%2C%22x_object_type%22%3A%22item%22%2C%22x_sku_id%22%3A%220%22%2C%22pageIndex%22%3A%220%22%2C%22pagePos%22%3A%229%22%2C%22pageSize%22%3A%2260%22%2C%22listno%22%3A%229%22%2C%22sort%22%3A%22priceasc%22%2C%22searchScenario%22%3A%22keyword%22%2C%22query%22%3A%22%EB%82%98%EC%9D%B4%ED%82%A4%20%EC%97%90%EC%96%B4%ED%8F%AC%EC%8A%A4%20cw2288-111%22%2C%22pvid%22%3A%22fcfb553ceb9b49d7942555b096237408%22%2C%22pvid_sys%22%3A%22gmarket%20server%22%2C%22search_session_id%22%3A%2224980bab-e87b-4841-b5df-f941c70fb851%22%2C%22origin_price%22%3A%22146000%22%2C%22promotion_price%22%3A%22131400%22%2C%22coupon_price%22%3A%22124830%22%2C%22ab_buckets%22%3A%22%22%2C%22trafficType%22%3A%22organic%22%7D'

parser = GmarketReviewParser("image")

list = parser.get_reviews(link)

# todo: dict 결과값에 대표상품 id 추가


# todo: 카프카에 발행 (topic - product_details)