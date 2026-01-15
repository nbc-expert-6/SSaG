package com.sparta.productservice.common.kafka.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaTopicType;
import com.sparta.productservice.common.kafka.message.DLTMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DLTPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	/**
	 * Exception 객체로 DLQ 전송
	 */
	public void sendToDLT(KafkaTopicType topic, String key, Object payload, Exception error, int retryCount) {
		DLTMessage message = DLTMessage.of(topic.dlt(), key, payload, error, retryCount);
		sendDLTMessage(topic.dlt(), key, message);
	}

	/**
	 * 실제 DLQ 전송 로직
	 */
	private void sendDLTMessage(String topic, String key, DLTMessage message) {
		try {
			kafkaTemplate.send(topic, key, message)
				.whenComplete((result, ex) -> {
					if (ex != null) {
						log.error("❌ Failed to send to DLT topic={}, key={}", topic, key, ex);
					} else {
						log.info("✅ Sent to DLT: topic={}, key={}", topic, key);
					}
				});
		} catch (Exception e) {
			log.error("❌ Critical error sending to DLQ: topic={}, key={}", topic, key, e.getMessage());
		}
	}
}