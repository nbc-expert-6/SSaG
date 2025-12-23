package com.sparta.productservice.review.infra.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaConsumerConstants;
import com.sparta.productservice.common.kafka.KafkaTopicType;
import com.sparta.productservice.common.kafka.consumer.AbstractKafkaConsumer;
import com.sparta.productservice.review.infra.event.handler.CrawledReviewHandler;
import com.sparta.productservice.review.infra.event.message.CrawledReviewMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledReviewConsumer extends AbstractKafkaConsumer<CrawledReviewMessage> {

	private final CrawledReviewHandler handler;

	@RetryableTopic(
		attempts = KafkaConsumerConstants.MAX_ATTEMPT_COUNT_STR,
		backoff = @Backoff(value = KafkaConsumerConstants.RETRY_DELAY_MS, multiplier = KafkaConsumerConstants.BACKOFF_MULTIPLIER),
		dltStrategy = DltStrategy.FAIL_ON_ERROR,
		topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
	)
	@KafkaListener(
		topics = KafkaTopicType.Topics.PRODUCT_REVIEWS,
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "crawledReviewKafkaListenerContainerFactory"
	)
	public void consume(CrawledReviewMessage message) {
		log.info("📥 Consumed Review: mainProductId={}", message.mainProductId());

		handler.handleCrawledReview(message);

		log.info("✅ Successfully processed review: mainProductId={}", message.mainProductId());
	}
}