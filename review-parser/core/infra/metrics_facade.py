# core/infra/metrics_facade.py
from __future__ import annotations

from common.monitoring.metrics import (CRAWL_EXCEPTION_COUNT, CRAWL_LATENCY,
                                       CRAWL_TRIAL_COUNT, KAFKA_PUBLISH_COUNT,
                                       KAFKA_PUBLISH_LATENCY)


class MetricsFacade:
    def __init__(self, platform: str):
        self.platform = platform

    def inc_trial(self) -> None:
        CRAWL_TRIAL_COUNT.labels(platform=self.platform).inc()

    def observe_crawl_latency(self, seconds: float) -> None:
        CRAWL_LATENCY.labels(platform=self.platform).observe(seconds)

    def inc_exception(self) -> None:
        CRAWL_EXCEPTION_COUNT.labels(platform=self.platform).inc()

    def inc_publish_count(self) -> None:
        KAFKA_PUBLISH_COUNT.inc()

    def observe_publish_latency(self, seconds: float) -> None:
        KAFKA_PUBLISH_LATENCY.observe(seconds)
