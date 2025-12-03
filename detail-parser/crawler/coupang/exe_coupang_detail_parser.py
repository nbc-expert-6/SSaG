from crawler.coupang.coupang_detail_parser import CoupangDetailParser


links = 'https://www.coupang.com/vp/products/8274944625?itemId=23851886225&vendorItemId=90875266332&sourceType=srp_product_ads&clickEventId=0c9ddb20-cc05-11f0-92d2-4eef1827556a&korePlacement=15&koreSubPlacement=1&clickEventId=0c9ddb20-cc05-11f0-92d2-4eef1827556a&korePlacement=15&koreSubPlacement=1'
# links = 'https://www.coupang.com/vp/products/6957293598?itemId=18862945807&vendorItemId=94091193435&q=%EB%82%98%EC%9D%B4%ED%82%A4+%EC%97%90%EC%96%B4%ED%8F%AC%EC%8A%A4+cw2288-111&searchId=c905b21f717766&sourceType=search&itemsCount=36&searchRank=1&rank=1'
# links = 'https://www.coupang.com/vp/products/6602975280?itemId=14942230691&vendorItemId=81564489580&q=%EB%A7%88%EC%83%AC+%EC%95%B0%EB%B2%84%ED%8A%BC&searchId=0baee43d1553021&sourceType=search&itemsCount=36&searchRank=50&rank=50&traceId=miihqons'

parser = CoupangDetailParser()

parser.get_product_details(links)
parser.driver.quit()