from kafka import KafkaProducer, KafkaConsumer
import json

# Kafka Producer 설정
def create_producer(servers='localhost:9092'):
    return KafkaProducer(
        bootstrap_servers=servers,
        value_serializer=lambda v: json.dumps(v).encode('utf-8')
    )

# Kafka Consumer 설정
def create_consumer(topic, servers='localhost:9092', group_id=None):
    return KafkaConsumer(
        topic,
        bootstrap_servers=servers,
        value_deserializer=lambda v: json.loads(v.decode('utf-8')),
        auto_offset_reset='earliest',
        group_id=group_id
    )
