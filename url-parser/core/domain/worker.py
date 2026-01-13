# core/domain/worker.py
from __future__ import annotations

import time
from typing import Callable, List, Protocol

from core.domain.models import CrawlInput, CrawlResult
from core.infra.metrics_facade import MetricsFacade

from common.exceptions import UrlParseException


# 프로토콜 명시 - 어던 것들 쓸것인지 알려주는 용도
class LoggerProtocol(Protocol):
    def info(self, event: str, **kw): ...
    def error(self, event: str, **kw): ...
    def perf(self, event: str, **kw): ...


class ParserProtocol(Protocol):
    def get_product_urls(self, keyword: str) -> List[str]: ...
    def quit(self) -> None: ...


class UrlCrawlWorker:
    def __init__(self, platform: str, logger: LoggerProtocol, metrics: MetricsFacade):
        self.platform = platform
        self.logger = logger
        self.metrics = metrics

    def crawl(self, parser: ParserProtocol, inp: CrawlInput) -> CrawlResult:
        self.metrics.inc_trial()
        start = time.perf_counter()

        try:
            urls = parser.get_product_urls(inp.keyword)
            elapsed = time.perf_counter() - start

            self.logger.info("SEARCH_COMPLETED", keyword=str(inp.keyword), url_cnt=len(urls))
            if len(urls) < 1:
                self.logger.info("SEARCH_NO_RESULT", keyword=str(inp.keyword))

            self.logger.perf("PARSING_COMPLETED", url_cnt=len(urls), time=round(elapsed, 4))
            self.metrics.observe_crawl_latency(elapsed)

            return CrawlResult(urls=urls, url_cnt=len(urls), elapsed_sec=elapsed)

        except Exception as e:
            elapsed = time.perf_counter() - start

            raise UrlParseException(
                main_product_id=inp.main_product_id,
                keyword=str(inp.keyword),
                elapsed= elapsed,
                exception=e,
            )
