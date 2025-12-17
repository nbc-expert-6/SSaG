package com.sparta.productservice.common.kafka.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.message.DLQMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DLQPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	/**
	 * Exception 객체로 DLQ 전송
	 */
	public void sendToDLQ(String topic, String key, Object payload, Exception error, int retryCount) {
		DLQMessage dlqMessage = DLQMessage.of(topic, key, payload, error, retryCount);
		sendDLQMessage(topic, key, dlqMessage);
	}

	/**
	 * 실제 DLQ 전송 로직
	 */
	private void sendDLQMessage(String topic, String key, DLQMessage dlqMessage) {
		try {
			String dlqTopic = topic + ".dlq";

			kafkaTemplate.send(dlqTopic, key, dlqMessage)
				.whenComplete((result, ex) -> {
					if (ex != null) {
						log.error("❌ Failed to send to DLQ topic={}, key={}", dlqTopic, key, ex);
					} else {
						log.info("✅ Sent to DLQ: topic={}, key={}, messageId={}",
							dlqTopic, key, dlqMessage.messageId());
					}
				});
		} catch (Exception e) {
			log.error("❌ Critical error sending to DLQ: topic={}, key={}", topic, key, e);
		}
	}
}
