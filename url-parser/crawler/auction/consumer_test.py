from ...common.kafka_utils import create_consumer

consumer = create_consumer(
    topic='auction_links',
    group_id='test-group'
)

for message in consumer:
    print(message.value)
