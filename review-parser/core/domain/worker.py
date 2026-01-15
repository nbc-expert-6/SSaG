# core/domain/worker.py
from __future__ import annotations

import time
from typing import Any, Dict, Protocol

from core.domain.models import ReviewInput, ReviewResult
from core.infra.metrics_facade import MetricsFacade

from common.exceptions import ReviewParseException


class LoggerProtocol(Protocol):
    def info(self, event: str, **kw): ...
    def error(self, event: str, **kw): ...
    def exception(self, event: str, **kw): ...
    def perf(self, event: str, **kw): ...


class ParserProtocol(Protocol):
    def get_reviews(self, url: str) -> list[Dict[str, Any]]: ...
    def quit(self) -> None: ...


class ReviewWorker:
    def __init__(self, platform: str, logger: LoggerProtocol, metrics: MetricsFacade):
        self.platform = platform
        self.logger = logger
        self.metrics = metrics

    def crawl_one(self, parser: ParserProtocol, inp: ReviewInput) -> ReviewResult:
        start = time.perf_counter()
        self.metrics.inc_trial()

        try:
            reviews = parser.get_reviews(inp.url)

            elapsed = time.perf_counter() - start
            self.metrics.observe_crawl_latency(elapsed)
            self.logger.perf("PARSING_COMPLETED", time=round(elapsed, 4))

            if not reviews:
                return ReviewResult(ok=True, elapsed_sec=elapsed)

            payload = {
                "main_product_id": inp.main_product_id,
                "platform": self.platform,
                "reviews": reviews,
            }

            return ReviewResult(ok=True, elapsed_sec=elapsed, payload=payload)

        except ReviewParseException as e:
            elapsed = time.perf_counter() - start
            self.metrics.observe_crawl_latency(elapsed)
            self.metrics.inc_exception()

            self.logger.error(
                "REVIEW_PARSE_FAILED",
                main_product_id=inp.main_product_id,
                url=inp.url,
                stage=e.stage,
                reason=e.reason,
                original_exception_type=e.original_exception_type,
                elapsed=round(elapsed, 4),
                exc_info=True,
            )

            return ReviewResult(
                ok=False,
                elapsed_sec=elapsed,
                error_type=e.original_exception_type,
                error_message=str(e),
                stage=e.stage,
                reason=e.reason,
            )

        except Exception as e:
            elapsed = time.perf_counter() - start
            self.metrics.observe_crawl_latency(elapsed)
            self.metrics.inc_exception()

            self.logger.exception(
                "UNEXPECTED_EXCEPTION",
                main_product_id=inp.main_product_id,
                url=inp.url,
                original_exception_type=type(e).__name__,
                exc_info=True,
            )

            return ReviewResult(
                ok=False,
                elapsed_sec=elapsed,
                error_type=type(e).__name__,
                error_message=str(e),
            )