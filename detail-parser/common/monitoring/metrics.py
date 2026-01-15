from prometheus_client import Counter, Histogram


# 지수 증가 bucket 배열 생성 함수
def exponential_buckets(start, factor, count):
    return [start * (factor ** i) for i in range(count)]

CRAWL_TRIAL_COUNT = Counter(
    "crawl_trial_total",
    "개별 크롤러 크롤링 시도 횟수",
    ["platform"]
)

CRAWL_EXCEPTION_COUNT = Counter(
    "crawl_exception_total",
    "개별 크롤러에서 발생한 예외 횟수",
    ["platform"]
)

CRAWL_LATENCY = Histogram(
    "crawl_latency_seconds",
    "개별 크롤링에 소요된 시간(초) 분포",
    ["platform"],
    buckets= exponential_buckets(start=0.1, factor=2, count=10)
)

KAFKA_PUBLISH_COUNT = Counter(
    "kafka_publish_total",
    "Kafka 메시지 전송 횟수"
)

KAFKA_PUBLISH_LATENCY = Histogram(
    "kafka_publish_latency_seconds",
    "Kafka 메시지 전송에 소요된 시간(초)",
    buckets=[0.01, 0.05, 0.1, 0.3, 0.5, 1]
)
