class ReviewParseException(Exception):
    def __init__(
        self,
        stage: str,
        reason: str,
        original_exception: Exception,
        original_exception_type: str,
    ):
        self.stage = stage
        self.reason = reason
        self.original_exception = original_exception
        self.original_exception_type = original_exception_type

        super().__init__(
            f"[{stage}] {reason} ({original_exception_type})"
        )
