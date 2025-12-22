package com.sparta.productservice.common.kafka.consumer;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractKafkaConsumer<T> {

	@DltHandler
	public void handleDlt(
		T message,
		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
		@Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage
	) {
		log.error(
			"❌ DLQ 처리: topic={}, payload={}, error={}",
			topic,
			message,
			exceptionMessage
		);
	}
}
