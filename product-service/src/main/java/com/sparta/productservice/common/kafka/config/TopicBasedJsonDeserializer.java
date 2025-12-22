package com.sparta.productservice.common.kafka.config;

import org.apache.commons.lang3.SerializationException;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.productservice.common.kafka.KafkaTopicType;

public class TopicBasedJsonDeserializer extends JsonDeserializer<Object> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Object deserialize(String topic, byte[] data) {
		if (data ==

		try {
			KafkaTopicType topicType = KafkaTopicType.fromTopic(topic);
			Class<?> targetClass = topicType.valueClass();
			return objectMapper.readValue(data, targetClass);

		} catch (IllegalArgumentException e) {
			throw new SerializationException(
				"매핑되지 않은 Kafka topic: " + topic, e
			);null) {
				return null;
			}
		} catch (Exception e) {
			throw new SerializationException(
				"Kafka deserialize 실패. topic=" + topic, e
			);
		}
	}
}
