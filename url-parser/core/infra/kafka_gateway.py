# core/infra/kafka_gateway.py
from __future__ import annotations

from typing import Any, Dict, Iterable, Optional

from common.kafka_utils import create_consumer, create_producer


class KafkaGateway:
    def __init__(self, consume_topic: str, group_id: str, produce_topic: str, dlq_topic: str) -> None:
        self.consume_topic = consume_topic
        self.group_id = group_id
        self.produce_topic = produce_topic
        self.dlq_topic = dlq_topic

        self.consumer = create_consumer(topic=consume_topic, group_id=group_id)
        self.producer = create_producer()

    # kafka gateway를 iterable하게 설정 -> runner 코드에서 " for msg in self.kafka: ~ "
    def __iter__(self) -> Iterable[Any]:
        return iter(self.consumer)

    def commit(self) -> None:
        self.consumer.commit()

    def publish(self, main_product_id: str, urls: list[str]) -> None:
        topic = self.dlq_topic if urls is None else self.produce_topic

        self.producer.send(
            topic,
            {"main_product_id": main_product_id, "urls": urls},
        )

    def flush(self) -> None:
        self.producer.flush()

    def close(self) -> None:
        try:
            self.producer.close()
        except Exception:
            pass
