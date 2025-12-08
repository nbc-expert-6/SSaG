package com.sparta.productservice.review.infra.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.sparta.productservice.review.infra.event.handler.CrawledReviewHandler;
import com.sparta.productservice.review.infra.event.message.CrawledReviewMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledReviewConsumer {

	private final CrawledReviewHandler handler;

	@KafkaListener(
		topics = "product-reviews",
		groupId = "${spring.kafka.consumer.group-id}"
	)
	public void consume(CrawledReviewMessage message, Acknowledgment ack) {
		log.info("Consumed Review: mainProductId={}", message.mainProductId());

		try {
			handler.handleCrawledReview(message);
			ack.acknowledge();
		} catch (Exception e) {
			log.error("Failed to process: {}", e.getMessage(), e);
		}
	}
}