from __future__ import annotations

from core.app.restart_policy import BatchRestartPolicy
from core.app.runner import UrlParserRunner
from core.domain.worker import UrlCrawlWorker
from core.infra.browser_manager import BrowserManager
from core.infra.kafka_gateway import KafkaGateway
from core.infra.metrics_facade import MetricsFacade
from core.infra.url_parser_resolver import UrlParserResolver

from common.config import BATCH_SIZE, PLATFORM
from common.logging_utils import setup_dev_logger
from common.monitoring.logger import setup_logger
from common.monitoring.metrics_server import start_metrics_server


def main() -> None:
    platform = PLATFORM

    # logging
    setup_dev_logger()
    logger = setup_logger(platform)

    # metrics server
    start_metrics_server()

    # config
    max_batch = int(BATCH_SIZE)

    # infra
    kafka = KafkaGateway(
        consume_topic="keywords",
        group_id=platform+"-url-group",
        produce_topic=platform+"-product-urls",
        dlq_topic=platform+"-url.dlq"
    )
    metrics = MetricsFacade(platform=platform)

    # parser 매핑시켜주는 작업
    # 플랫폼을 넘가면 이름기반으로 파서 클래스 찾아서 반환
    parser = UrlParserResolver.resolve(platform)

    # browser / parser
    browser = BrowserManager(parser)

    # domain
    worker = UrlCrawlWorker(platform=platform, logger=logger, metrics=metrics)
    policy = BatchRestartPolicy(max_batch=max_batch)

    # runner
    runner = UrlParserRunner(
        platform=platform,
        logger=logger,
        kafka=kafka,
        browser=browser,
        worker=worker,
        restart_policy=policy,
        metrics=metrics,
        sleep_after_restart_sec=1.0,
        flush_after_publish=False,  # 필요하면 True (대신 성능 영향)
    )

    runner.run()

if __name__ == "__main__":
    main()
