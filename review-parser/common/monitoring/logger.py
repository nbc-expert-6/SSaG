import logging
import sys
from typing import Any

import structlog

from common.config import ENVIRONMENT

"""
    운영 단계 모니터링 로그 설정입니다.
    아래 사용 예시를 참고해주세요.
    
    [logger 세팅]
    logger = setup_logger("platform")
    
    [로깅]
    logger.{info/perf/warning/error}("EVENT_TYPE", field=field)
"""

PERF_LEVEL_NUM = 25
logging.addLevelName(PERF_LEVEL_NUM, "PERF")
structlog.stdlib.LEVEL_TO_NAME[PERF_LEVEL_NUM] = "perf"

def perf(self, message, *args, **kwargs):
    if self.isEnabledFor(PERF_LEVEL_NUM):
        self._log(PERF_LEVEL_NUM, message, args, **kwargs)

logging.Logger.perf = perf

# PERF 레벨 지원하는 BoundLogger 설정
class CustomBoundLogger(structlog.stdlib.BoundLogger):
    def perf(self, event: str = None, **kw: Any) -> Any:
        # 성능 측정 관련 로그
        return self.log(PERF_LEVEL_NUM, event, **kw)

def setup_logger(platform: str):

    # 기본 logging 설정
    logging.basicConfig(
        format="%(message)s",
        stream=sys.stdout,
        level=logging.INFO,
    )

    structlog.configure(
        processors=[
            # 로그 레벨
            structlog.stdlib.add_log_level,
            # 타임스탬프
            structlog.processors.TimeStamper(fmt="iso"),
            # 스택 정보 (에러 발생 경우)
            structlog.processors.StackInfoRenderer(),
            # Exception 정보 포맷팅
            structlog.processors.format_exc_info,
            # 출력 형식 (json)
            structlog.processors.JSONRenderer()
        ],
        context_class=dict,
        logger_factory=structlog.stdlib.LoggerFactory(),
        wrapper_class=CustomBoundLogger,    # type: ignore[arg-type]
        cache_logger_on_first_use=True,
    )

    # 기본 컨텍스트 설정
    logger = structlog.get_logger()
    logger = logger.bind(
        platform=platform,
        environment=ENVIRONMENT
    )

    return logger
