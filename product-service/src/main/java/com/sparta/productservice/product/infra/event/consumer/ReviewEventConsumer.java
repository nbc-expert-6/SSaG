package com.sparta.productservice.product.infra.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.sparta.productservice.product.infra.event.handler.ReviewEventHandler;
import com.sparta.productservice.common.message.ReviewCreatedMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventConsumer {
	private final ReviewEventHandler handler;

	@KafkaListener(
		topics = "review.created.events",
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void consume(ReviewCreatedMessage message, Acknowledgment ack) {
		log.info(message.toString());
		log.info("Consumed Review events: mainProductIds={}", message.mainProductId());

		try {
			handler.handleReviewCreated(message);
			ack.acknowledge();
		} catch (Exception e) {
			log.error("Failed to process review events: {}", e.getMessage(), e);
		}
	}
}