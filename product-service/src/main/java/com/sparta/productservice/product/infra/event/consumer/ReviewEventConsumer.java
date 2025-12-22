package com.sparta.productservice.product.infra.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaTopicType;
import com.sparta.productservice.common.kafka.consumer.AbstractKafkaConsumer;
import com.sparta.productservice.common.kafka.message.ReviewCreatedMessage;
import com.sparta.productservice.product.infra.event.handler.ReviewEventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventConsumer extends AbstractKafkaConsumer<ReviewCreatedMessage> {
	private final ReviewEventHandler handler;

	@KafkaListener(
		topics = KafkaTopicType.Topics.REVIEW_CREATED,
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void consume(ReviewCreatedMessage message) {
		log.info("📥 Consumed Review event: mainProductId={}", message.mainProductId());

		handler.handleReviewCreated(message);

		log.info("✅ Successfully processed review event: mainProductId={}", message.mainProductId());
	}
}