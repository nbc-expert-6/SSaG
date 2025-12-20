package com.sparta.productservice.product.infra.event.consumer;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaConsumerConstants;
import com.sparta.productservice.common.kafka.message.ReviewCreatedMessage;
import com.sparta.productservice.product.infra.event.handler.ReviewEventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventConsumer {
	private final ReviewEventHandler handler;

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
		topics = "review.created.events",
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void consume(ReviewCreatedMessage message, Acknowledgment ack) {
		log.info("📥 Consumed Review event: mainProductId={}", message.mainProductId());

		handler.handleReviewCreated(message);
		ack.acknowledge();

		log.info("✅ Successfully processed review event: mainProductId={}", message.mainProductId());
	}

	@DltHandler
	public void handleDlt(
		ReviewCreatedMessage message,
		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
		@Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage,
		@Header(KafkaHeaders.EXCEPTION_STACKTRACE) String stackTrace
	) {
		log.error("❌ All retries failed, handling DLQ: topic={}, mainProductId={}",
			topic, message.mainProductId());
	}
}