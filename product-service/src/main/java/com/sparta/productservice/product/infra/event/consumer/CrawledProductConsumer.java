package com.sparta.productservice.product.infra.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.sparta.productservice.common.kafka.KafkaConsumerConstants;
import com.sparta.productservice.common.kafka.KafkaTopicType;
import com.sparta.productservice.common.kafka.consumer.AbstractKafkaConsumer;
import com.sparta.productservice.product.infra.event.handler.CrawledProductHandler;
import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledProductConsumer extends AbstractKafkaConsumer<CrawledProductMessage> {

	private final CrawledProductHandler handler;

	@RetryableTopic(
		attempts = KafkaConsumerConstants.MAX_ATTEMPT_COUNT_STR,
		backoff = @Backoff(KafkaConsumerConstants.RETRY_DELAY_MS),
		dltStrategy = DltStrategy.FAIL_ON_ERROR,
		topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
	)
	@KafkaListener(
		topics = KafkaTopicType.Topics.PRODUCT_DETAILS,
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "crawledProductKafkaListenerContainerFactory"
	)
	public void consume(CrawledProductMessage message) {
		log.info("📥 Consumed Product: mainProductId={}, url={}",
			message.mainProductId(), message.saleLink());

		handler.handleCrawledProduct(message);

		log.info("✅ Successfully processed product: mainProductId={}",
			message.mainProductId());
	}
}
