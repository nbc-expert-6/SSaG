from crawler.gmarket.gmarket_detail_parser import GmarketDetailParser

# todo: 카프카 컨슈머로 link데이터와 대표상품id 받아오기

link = 'https://item.gmarket.co.kr/Item?goodscode=2826752588&utparam-url=%7B%22scene%22%3A%22search%22%2C%22sub_scene%22%3A%22main_srp%22%2C%22x_object_id%22%3A%222826752588%22%2C%22x_object_type%22%3A%22item%22%2C%22x_sku_id%22%3A%220%22%2C%22pageIndex%22%3A%220%22%2C%22pagePos%22%3A%220%22%2C%22pageSize%22%3A%224%22%2C%22listno%22%3A%220%22%2C%22sort%22%3A%22popularity%22%2C%22searchScenario%22%3A%22keyword%22%2C%22query%22%3A%22%EB%82%98%EC%9D%B4%ED%82%A4%20%EC%97%90%EC%96%B4%ED%8F%AC%EC%8A%A4%20cw2288-111%22%2C%22pvid%22%3A%225db23ef58b6444d7af295be8c712b5eb%22%2C%22pvid_sys%22%3A%22gmarket%20server%22%2C%22search_session_id%22%3A%2233e5b388-2ad5-4b97-a6b8-dd129433d660%22%2C%22origin_price%22%3A%22149000%22%2C%22promotion_price%22%3A%22141640%22%2C%22coupon_price%22%3A%22134560%22%2C%22ab_buckets%22%3A%22%2399%5E2%23B%22%2C%22trafficType%22%3A%22organic%22%7D'
# link = 'https://item.gmarket.co.kr/Item?spm=gmktpc.searchlist.organictier.d0_24.8dc77f35E5j79Y&goodscode=4335931636&utparam-url=%7B%22scene%22%3A%22search%22%2C%22sub_scene%22%3A%22main_srp%22%2C%22x_object_id%22%3A%224335931636%22%2C%22x_object_type%22%3A%22item%22%2C%22x_sku_id%22%3A%220%22%2C%22pageIndex%22%3A%220%22%2C%22pagePos%22%3A%2224%22%2C%22pageSize%22%3A%2260%22%2C%22listno%22%3A%2224%22%2C%22sort%22%3A%22priceasc%22%2C%22searchScenario%22%3A%22keyword%22%2C%22query%22%3A%22%EB%82%98%EC%9D%B4%ED%82%A4%20%EC%97%90%EC%96%B4%ED%8F%AC%EC%8A%A4%20cw2288-111%22%2C%22pvid%22%3A%22fcfb553ceb9b49d7942555b096237408%22%2C%22pvid_sys%22%3A%22gmarket%20server%22%2C%22search_session_id%22%3A%2224980bab-e87b-4841-b5df-f941c70fb851%22%2C%22origin_price%22%3A%22138000%22%2C%22promotion_price%22%3A%22%22%2C%22coupon_price%22%3A%22%22%2C%22ab_buckets%22%3A%22%22%2C%22trafficType%22%3A%22organic%22%7D'
# link = 'https://item.gmarket.co.kr/Item?spm=gmktpc.searchlist.organictier.d0_55.8dc77f35E5j79Y&goodscode=4514357872&utparam-url=%7B%22scene%22%3A%22search%22%2C%22sub_scene%22%3A%22main_srp%22%2C%22x_object_id%22%3A%224514357872%22%2C%22x_object_type%22%3A%22item%22%2C%22x_sku_id%22%3A%220%22%2C%22pageIndex%22%3A%220%22%2C%22pagePos%22%3A%2255%22%2C%22pageSize%22%3A%2260%22%2C%22listno%22%3A%2255%22%2C%22sort%22%3A%22priceasc%22%2C%22searchScenario%22%3A%22keyword%22%2C%22query%22%3A%22%EB%82%98%EC%9D%B4%ED%82%A4%20%EC%97%90%EC%96%B4%ED%8F%AC%EC%8A%A4%20cw2288-111%22%2C%22pvid%22%3A%22fcfb553ceb9b49d7942555b096237408%22%2C%22pvid_sys%22%3A%22gmarket%20server%22%2C%22search_session_id%22%3A%2224980bab-e87b-4841-b5df-f941c70fb851%22%2C%22origin_price%22%3A%22144600%22%2C%22promotion_price%22%3A%22138820%22%2C%22coupon_price%22%3A%22%22%2C%22ab_buckets%22%3A%22%22%2C%22trafficType%22%3A%22organic%22%7D'

parser = GmarketDetailParser()

dict = parser.get_product_details(link)

# todo: 삭제하거나 로그로 변경
for key, value in dict.items():
    print(key, ":", value)

# todo: dict 결과값에 대표상품 id 추가


# todo: 카프카에 발행 (topic - product_details)