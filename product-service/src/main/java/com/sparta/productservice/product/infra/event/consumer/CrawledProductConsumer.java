package com.sparta.productservice.product.infra.event.consumer;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaConsumerConstants;
import com.sparta.productservice.product.infra.event.handler.CrawledProductHandler;
import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledProductConsumer {

	private final CrawledProductHandler handler;

	@RetryableTopic(
		attempts = KafkaConsumerConstants.MAX_ATTEMPT_COUNT,
		backoff = @Backoff(KafkaConsumerConstants.RETRY_DELAY_MS),
		dltStrategy = DltStrategy.FAIL_ON_ERROR,
		topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
	@KafkaListener(
		topics = "product-details",
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "crawledProductKafkaListenerContainerFactory"
	)
	public void consume(CrawledProductMessage message, Acknowledgment ack) {
		log.info("📥 Consumed Product: mainProductId={}, url={}",
			message.mainProductId(), message.saleLink());

		handler.handleCrawledProduct(message);

		log.info("✅ Successfully processed product: mainProductId={}",
			message.mainProductId());
	}

	@DltHandler
	public void handleDlt(
		CrawledProductMessage message,
		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
		@Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage,
		@Header(KafkaHeaders.EXCEPTION_STACKTRACE) String stackTrace
	) {
		log.error("❌ DLQ: topic={}, productId={}", topic, message.mainProductId());
	}
}
