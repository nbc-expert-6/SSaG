from crawler.coupang.coupang_url_parser import CoupangUrlParser

def main():
    # 테스트할 검색 키워드 리스트
    keywords = [
        "나이키 에어포스 cw2288-111",
        "나이키 에어포스 cw2288-001",
        "나이키 에어포스 cw2288-002"
    ]

    # 쿠팡 URL Parser 객체 생성
    parser = CoupangUrlParser("https://www.coupang.co.kr", 5)

    parser.open_main_page()
    try:
        for keyword in keywords:
            print(f"=== {keyword} 검색 시작 ===")
            parser.search(keyword)
            parser.sort_by_low_price()
            parser.remove_add()
            links = parser.get_product_links()
            print(links)
    finally:
        # 드라이버 종료
        parser.driver.quit()

if __name__ == "__main__":
    main()