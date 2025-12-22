import json

from kafka import KafkaConsumer, KafkaProducer

from common.config import KAFKA_BOOTSTRAP


# Kafka Producer 설정
def create_producer(servers=KAFKA_BOOTSTRAP):
    return KafkaProducer(
        bootstrap_servers=servers,
        value_serializer=lambda v: json.dumps(v).encode('utf-8')
    )

# Kafka Consumer 설정
def create_consumer(topic, servers=KAFKA_BOOTSTRAP, group_id=None):
    return KafkaConsumer(
        topic,
        bootstrap_servers=servers,
        value_deserializer=lambda v: json.loads(v.decode('utf-8')),
        auto_offset_reset='earliest',
        group_id=group_id,
        max_poll_interval_ms=1800000,
        session_timeout_ms=300000,
        heartbeat_interval_ms=10000,
        request_timeout_ms=1805000,
        connections_max_idle_ms=1805500,
        max_poll_records=1,
        enable_auto_commit=False
    )
