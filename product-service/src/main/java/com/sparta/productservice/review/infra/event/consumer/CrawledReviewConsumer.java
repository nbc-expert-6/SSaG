package com.sparta.productservice.review.infra.event.consumer;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaConsumerConstants;
import com.sparta.productservice.common.kafka.publisher.DLQPublisher;
import com.sparta.productservice.review.infra.event.handler.CrawledReviewHandler;
import com.sparta.productservice.review.infra.event.message.CrawledReviewMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledReviewConsumer {

	private final CrawledReviewHandler handler;
	private final DLQPublisher dlqPublisher;

	@RetryableTopic(
		attempts = KafkaConsumerConstants.MAX_ATTEMPT_COUNT,
		backoff = @Backoff(
			value = KafkaConsumerConstants.RETRY_DELAY_MS,
			multiplier = KafkaConsumerConstants.BACKOFF_MULTIPLIER
		),
		autoCreateTopics = "false",
		dltTopicSuffix = ".dlq",
		topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
	)
	@KafkaListener(
		topics = "product-reviews",
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "crawledReviewKafkaListenerContainerFactory"
	)
	public void consume(CrawledReviewMessage message) {
		log.info("📥 Consumed Review: mainProductId={}", message.mainProductId());

		handler.handleCrawledReview(message);

		log.info("✅ Successfully processed review: mainProductId={}", message.mainProductId());
	}

	@DltHandler
	public void handleDlt(
		CrawledReviewMessage message,
		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
		@Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage,
		@Header(KafkaHeaders.EXCEPTION_STACKTRACE) String stackTrace
	) {
		log.error("❌ All retries failed, handling DLQ: topic={}, mainProductId={}",
			topic, message.mainProductId());
	}
}