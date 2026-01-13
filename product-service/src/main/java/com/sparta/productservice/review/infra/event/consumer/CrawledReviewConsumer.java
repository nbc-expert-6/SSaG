package com.sparta.productservice.review.infra.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaTopicType;
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
		topics = KafkaTopicType.Topics.PRODUCT_REVIEWS,
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "crawledReviewKafkaListenerContainerFactory"
	)
	public void consume(CrawledReviewMessage message) {
		log.info("📥 Consumed Review: mainProductId={}", message.getMainProductId());

		handler.handleCrawledReview(message);

		log.info("✅ Successfully processed review: mainProductId={}", message.getMainProductId());
	}
}