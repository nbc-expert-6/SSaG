# core/app/runner.py
from __future__ import annotations

from concurrent.futures import ThreadPoolExecutor, as_completed

from core.app.restart_policy import PoolRestartPolicy
from core.domain.models import BatchMessage, ReviewInput
from core.infra.browser_manager import DriverPoolManager
from core.infra.kafka_gateway import KafkaGateway
from core.infra.metrics_facade import MetricsFacade


class ReviewParserRunner:
    def __init__(
        self,
        platform: str,
        logger,
        kafka: KafkaGateway,
        driver_pool: DriverPoolManager,
        worker,
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

    def run(self) -> None:
        self.driver_pool.init_pool()
        processed = 0

        with ThreadPoolExecutor(max_workers=self.max_workers) as executor:
            for raw_msg in self.kafka:
                batch = BatchMessage(
                    main_product_id=raw_msg.value["main_product_id"],
                    urls=raw_msg.value["urls"],
                    raw=raw_msg.value,
                )

                futures = []

                for url in batch.urls:
                    inp = ReviewInput(batch.main_product_id, url)

                    futures.append(
                        executor.submit(self._process_one, inp)
                    )

                for future in as_completed(futures):
                    result = future.result()

                    if result.ok and result.payload:
                        self.kafka.publish_success(result.payload)
                        self.metrics.inc_publish_count()
                    elif not result.ok:
                        self.kafka.publish_dlq({
                            "platform": self.platform,
                            "main_product_id": batch.main_product_id,
                            "error_type": result.error_type,
                            "error_message": result.error_message,
                            "stage": result.stage,
                            "reason": result.reason,
                        })

                    processed += 1

                    if self.restart_policy.should_restart(processed):
                        self.logger.info(
                            "DRIVER_POOL_RESTART",
                            reason=self.restart_policy.reason(processed),
                        )
                        self.driver_pool.shutdown_pool()
                        self.driver_pool.init_pool()

                if self.flush_after_batch:
                    self.kafka.flush()
                self.kafka.commit()

        self.driver_pool.shutdown_pool()

    def _process_one(self, inp: ReviewInput):
        with self.driver_pool.acquire() as parser:
            return self.worker.crawl_one(parser, inp)