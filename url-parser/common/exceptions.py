class UrlParseException(Exception):
    def __init__(
        self,
        main_product_id: str,
        keyword: str,
        elapsed: float,
        exception: Exception,
    ):
        self.main_product_id = main_product_id,
        self.keyword = keyword,
        self.elapsed = elapsed,
        self.exception = exception

        super().__init__(
            f"[{main_product_id}] ({exception})"
        )