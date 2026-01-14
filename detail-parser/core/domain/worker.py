from __future__ import annotations

import time
from typing import Any, Dict, Protocol

from core.domain.models import DetailInput, DetailResult
from core.infra.metrics_facade import MetricsFacade

from common.exceptions import DetailParseException


class LoggerProtocol(Protocol):
    def info(self, event: str, **kw): ...
    def error(self, event: str, **kw): ...
    def exception(self, event: str, **kw): ...
    def perf(self, event: str, **kw): ...


class ParserProtocol(Protocol):
    def get_product_details(self, url: str) -> Dict[str, Any]: ...
    def quit(self) -> None: ...


class ElevenstDetailWorker:
    def __init__(self, platform: str, logger: LoggerProtocol, metrics: MetricsFacade):
        self.platform = platform
        self.logger = logger
        self.metrics = metrics

    def crawl_one(self, parser: ParserProtocol, inp: DetailInput) -> DetailResult:
        start = time.perf_counter()
        self.metrics.inc_trial()

        try:
            product_details = parser.get_product_details(inp.url)
            product_details["main_product_id"] = inp.main_product_id
            product_details["platform"] = self.platform

            elapsed = time.perf_counter() - start
            self.metrics.observe_crawl_latency(elapsed)
            self.logger.perf("PARSING_COMPLETED", time=round(elapsed, 4))

            return DetailResult(ok=True, elapsed_sec=elapsed, payload=product_details)

        except DetailParseException as e:
            elapsed = time.perf_counter() - start
            self.metrics.observe_crawl_latency(elapsed)
            self.metrics.inc_exception()

            self.logger.error(
                "DETAIL_PARSE_FAILED",
                main_product_id=inp.main_product_id,
                url=inp.url,
                stage=e.stage,
                reason=e.reason,
                original_exception_type=e.original_exception_type,
                elapsed=round(elapsed, 4),
                exc_info=True,
            )

            return DetailResult(
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

            return DetailResult(
                ok=False,
                elapsed_sec=elapsed,
                error_type=type(e).__name__,
                error_message=str(e),
            )
