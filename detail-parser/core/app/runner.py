# core/app/runner.py
from __future__ import annotations

import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Any, Dict, List

from core.app.restart_policy import PoolRestartPolicy
from core.domain.models import BatchMessage, DetailInput
from core.domain.worker import ElevenstDetailWorker
from core.infra.browser_manager import DriverPoolManager
from core.infra.kafka_gateway import KafkaGateway
from core.infra.metrics_facade import MetricsFacade


class DetailParserRunner:
    def __init__(
        self,
        platform: str,
        logger,
        kafka: KafkaGateway,
        driver_pool: DriverPoolManager,
        worker: ElevenstDetailWorker,
        metrics: MetricsFacade,
        max_workers: int,
        restart_policy: PoolRestartPolicy,
        flush_after_batch: bool = True,
    ):
        self.platform = platform
        self.logger = logger
        self.kafka = kafka
        self.driver_pool = driver_pool
        self.worker = worker
        self.metrics = metrics
        self.max_workers = max_workers
        self.restart_policy = restart_policy
        self.flush_after_batch = flush_after_batch

        self.processed_batch_count = 0
        self.executor = ThreadPoolExecutor(max_workers=max_workers)

    def _maybe_restart_pool(self) -> None:
        if self.restart_policy.should_restart(self.processed_batch_count):
            reason = self.restart_policy.reason(self.processed_batch_count)
            self.logger.info("RESTART_DRIVER_POOL", reason=reason)
            self.driver_pool.shutdown_pool()
            self.driver_pool.init_pool()

    def _parse_batch_message(self, value: Dict[str, Any]) -> BatchMessage:
        return BatchMessage(
            main_product_id=str(value["main_product_id"]),
            urls=list(value["urls"]),
            raw=value,
        )

    def _submit_tasks(self, main_product_id: str, urls: List[str]):
        futures = []
        for url in urls:
            futures.append(
                self.executor.submit(self._run_one, DetailInput(main_product_id=main_product_id, url=url))
            )
        return futures

    def _run_one(self, inp: DetailInput):
        # driver 대여랑 반납을 runner에서 관리
        with self.driver_pool.acquire() as parser:
            return inp, self.worker.crawl_one(parser, inp)

    def run(self) -> None:
        total_start = time.perf_counter()

        self.driver_pool.init_pool()
        try:
            for msg in self.kafka:
                value = msg.value
                self.logger.info("KAFKA_CONSUME", value=value)

                self._maybe_restart_pool()

                batch = self._parse_batch_message(value)
                futures = self._submit_tasks(batch.main_product_id, batch.urls)

                for fut in as_completed(futures):
                    inp, result = fut.result()

                    # 수정 필요
                    if result.ok and result.payload:
                        # 성공
                        publish_start = time.perf_counter()
                        self.kafka.publish_success(result.payload)
                        self.metrics.observe_publish_latency(time.perf_counter() - publish_start)
                        self.metrics.inc_publish_count()
                    else:
                        # DLQ
                        # TODO: kafka 발행 관련 수정 필요
                        dlq_payload = {
                            "platform": self.platform,
                            "main_product_id": inp.main_product_id,
                            "url": inp.url,
                            "stage": result.stage,
                            "reason": result.reason,
                            "original_exception_type": result.error_type,
                            "exception_message": result.error_message,
                        }
                        self.kafka.publish_dlq(dlq_payload)

                if self.flush_after_batch:
                    self.kafka.flush()

                self.kafka.commit()
                self.processed_batch_count += 1

        finally:
            self.executor.shutdown(wait=True)
            self.driver_pool.shutdown_pool()
            self.kafka.close()

            total_elapsed = time.perf_counter() - total_start
            self.logger.info("PROGRAM_EXITED", total_time=round(total_elapsed, 4))
