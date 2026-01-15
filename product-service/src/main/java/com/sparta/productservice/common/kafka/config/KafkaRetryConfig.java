package com.sparta.productservice.common.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;

import com.sparta.productservice.common.kafka.KafkaConsumerConstants;
import com.sparta.productservice.common.kafka.KafkaTopicType;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaRetryConfig {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	@Bean
	public RetryTopicConfiguration retryTopicConfiguration() {
		return RetryTopicConfigurationBuilder
			.newInstance()

			.maxAttempts(KafkaConsumerConstants.MAX_ATTEMPT_COUNT)

			.exponentialBackoff(
				KafkaConsumerConstants.RETRY_DELAY_MS,
				KafkaConsumerConstants.BACKOFF_MULTIPLIER,
				KafkaConsumerConstants.MAX_RETRY_DELAY_MS
			)

			.suffixTopicsWithIndexValues()

			.dltSuffix(KafkaTopicType.Topics.DLT_TOPIC_SUFFIX)

			.dltProcessingFailureStrategy(DltStrategy.FAIL_ON_ERROR)

			.retryOn(Exception.class)

			.create(kafkaTemplate);

	}
}