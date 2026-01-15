# core/app/runner.py
from __future__ import annotations

import time

from core.app.restart_policy import BatchRestartPolicy
from core.domain.models import CrawlInput
from core.domain.worker import UrlCrawlWorker
from core.infra.browser_manager import BrowserManager
from core.infra.kafka_gateway import KafkaGateway
from core.infra.metrics_facade import MetricsFacade

from common.exceptions import UrlParseException


class UrlParserRunner:
    def __init__(
        self,
        platform: str,
        logger,
        kafka: KafkaGateway,
        browser: BrowserManager,
        worker: UrlCrawlWorker,
        restart_policy: BatchRestartPolicy,
        metrics: MetricsFacade,
        sleep_after_restart_sec: float = 1.0,
        flush_after_publish: bool = False,  # 필요 시 True
    ):
        self.platform = platform
        self.logger = logger
        self.kafka = kafka
        self.browser = browser
        self.worker = worker
        self.restart_policy = restart_policy
        self.metrics = metrics
        self.sleep_after_restart_sec = sleep_after_restart_sec
        self.flush_after_publish = flush_after_publish

        self.processed_count = 0

    def _maybe_restart_browser(self) -> None:
        if self.restart_policy.should_restart(self.processed_count):
            processed_count = self.restart_policy.get_processed_count(self.processed_count)
            self.browser.restart(processed_count=processed_count)
            time.sleep(self.sleep_after_restart_sec)

    def run(self) -> None:
        total_start = time.perf_counter()

        try:
            for msg in self.kafka:
                self.logger.info("KAFKA_CONSUME", value=f"{msg.value}")

                # 브라우저 재시작 확인
                self._maybe_restart_browser()

                value = msg.value
                inp = CrawlInput(
                    main_product_id=str(value.get("main_product_id")),
                    keyword=str(value.get("name")),
                    raw=value,
                )

                parser = self.browser.get()
                result = self.worker.crawl(parser, inp)

                # publish
                if result.url_cnt > 0:
                    publish_start = time.perf_counter()
                    self.kafka.publish(inp.main_product_id, result.urls)
                    publish_elapsed = time.perf_counter() - publish_start

                    self.metrics.observe_publish_latency(publish_elapsed)
                    self.metrics.inc_publish_count()

                    if self.flush_after_publish:
                        self.kafka.flush()

        except UrlParseException as e:
            self.logger.error(
                "PARSING_FAILED",
                main_product_id=e.main_product_id,
                keyword=str(e.keyword),
                exception=e.exception,
                exc_info=True,
            )

            self.logger.perf("PARSING_COMPLETED", time=round(e.elapsed, 4))
            self.metrics.observe_crawl_latency(e.elapsed)

            self.metrics.inc_exception()

            self.kafka.publish(
                main_product_id=e.main_product_id,
                urls=None)

        except Exception as e:
            self.logger.exception(
                "PARSING_FAILED",
                exception=e.exception,
                exc_info=True,
            )
            self.metrics.inc_exception()
            pass

        finally:
            # 성공, 실패 상관없이 다 commit
            self.kafka.commit()
            self.processed_count += 1

            # 종료 처리
            self.browser.quit()
            self.kafka.close()

            total_elapsed = time.perf_counter() - total_start
            self.logger.info("PROGRAM_EXITED", total_time=round(total_elapsed, 4))
