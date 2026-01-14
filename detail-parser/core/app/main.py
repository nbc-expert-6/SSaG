from __future__ import annotations

from core.app.restart_policy import PoolRestartPolicy
from core.app.runner import DetailParserRunner
from core.domain.worker import ElevenstDetailWorker
from core.infra.browser_manager import DriverPoolManager
from core.infra.kafka_gateway import KafkaGateway
from core.infra.metrics_facade import MetricsFacade
from core.infra.url_parser_resolver import DetailParserResolver

from common.config import PLATFORM
from common.monitoring.logger import setup_logger
from common.monitoring.metrics_server import start_metrics_server


def main() -> None:
    platform = PLATFORM

    logger = setup_logger(platform)
    start_metrics_server()

    # platform config
    max_workers = 3
    driver_pool_size = 3

    # infra
    kafka = KafkaGateway(
        consume_topic=platform+"-product-urls",
        group_id=platform+"-detail-group",
        produce_topic="product-details",
        dlq_topic=platform+"-detail.dlq"
    )
    metrics = MetricsFacade(platform=platform)

    # parser 매핑시켜주는 작업
    # 플랫폼을 넘가면 이름기반으로 파서 클래스 찾아서 반환
    parser = DetailParserResolver.resolve(platform)

    driver_pool = DriverPoolManager(
        parser_factory=parser,
        pool_size=driver_pool_size,
    )

    worker = ElevenstDetailWorker(platform=platform, logger=logger, metrics=metrics)

    # 필요 없으면 max_batches=0
    restart_policy = PoolRestartPolicy(max_batches=0)

    runner = DetailParserRunner(
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
