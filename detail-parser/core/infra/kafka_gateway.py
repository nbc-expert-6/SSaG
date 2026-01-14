# core/infra/kafka_gateway.py
from __future__ import annotations

from typing import Any, Dict, Iterable

from common.kafka_utils import create_consumer, create_producer


class KafkaGateway:
    def __init__(
        self,
        consume_topic: str,
        group_id: str,
        produce_topic: str,
        dlq_topic: str,
    ):
        self.consume_topic = consume_topic
        self.group_id = group_id
        self.produce_topic = produce_topic
        self.dlq_topic = dlq_topic

        self.consumer = create_consumer(topic=consume_topic, group_id=group_id)
        self.producer = create_producer()

    def __iter__(self) -> Iterable[Any]:
        return iter(self.consumer)

    def commit(self) -> None:
        self.consumer.commit()

    def publish_success(self, payload: Dict[str, Any]) -> None:
        self.producer.send(self.produce_topic, payload)

    def publish_dlq(self, payload: Dict[str, Any]) -> None:
        self.producer.send(self.dlq_topic, payload)

    def flush(self) -> None:
        self.producer.flush()

    def close(self) -> None:
        try:
            self.producer.close()
        except Exception:
            pass
