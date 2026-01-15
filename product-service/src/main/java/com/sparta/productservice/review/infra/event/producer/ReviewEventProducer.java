package com.sparta.productservice.review.infra.event.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.message.ReviewCreatedMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private static final String REVIEW_CREATED_TOPIC = "review.created.events";

	public void publishReviewCreatedEvents(ReviewCreatedMessage message) {
		kafkaTemplate.send(
			REVIEW_CREATED_TOPIC,
			message.mainProductId().toString(),
			message
		).whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("Failed to publish review events for product: {}",
					message.mainProductId(), ex);
			} else {
				log.info("Published {} review events for product: {}",message.reviews().size(), message.mainProductId());
			}
		});
	}
}
