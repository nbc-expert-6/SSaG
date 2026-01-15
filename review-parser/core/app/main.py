# main.py
from __future__ import annotations

from core.app.restart_policy import PoolRestartPolicy
from core.app.runner import ReviewParserRunner
from core.domain.worker import ReviewWorker
from core.infra.browser_manager import DriverPoolManager
from core.infra.kafka_gateway import KafkaGateway
from core.infra.metrics_facade import MetricsFacade
from core.infra.review_parser_resolver import ReviewParserResolver

from common.config import PLATFORM
from common.monitoring.logger import setup_logger
from common.monitoring.metrics_server import start_metrics_server


def main() -> None:
    platform = PLATFORM

    logger = setup_logger(platform)
    start_metrics_server()

    max_workers = 3
    driver_pool_size = 3

    kafka = KafkaGateway(
        consume_topic=f"{platform}-product-urls",
        group_id=f"{platform}-review-group",
        produce_topic="product-reviews",
        dlq_topic=f"{platform}-review.dlq",
    )

    metrics = MetricsFacade(platform=platform)

    parser_cls = ReviewParserResolver.resolve(platform)

    driver_pool = DriverPoolManager(
        parser_factory=parser_cls,
        pool_size=driver_pool_size,
    )

    worker = ReviewWorker(platform=platform, logger=logger, metrics=metrics)

    restart_policy = PoolRestartPolicy(max_batches=20)

    runner = ReviewParserRunner(
        platform=platform,
        logger=logger,
        kafka=kafka,
        driver_pool=driver_pool,
        worker=worker,
        metrics=metrics,
        max_workers=max_workers,
        restart_policy=restart_policy,
        flush_after_batch=True,
    )

    runner.run()


if __name__ == "__main__":
    main()