package com.sparta.productservice.common.kafka.config;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.backoff.FixedBackOff;

import com.sparta.productservice.common.kafka.KafkaConsumerConstants;
import com.sparta.productservice.common.kafka.KafkaTopicType;
import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;
import com.sparta.productservice.review.infra.event.message.CrawledReviewMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

	private final KafkaProperties kafkaProperties;

	@Bean
	public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
		KafkaTemplate<String, Object> kafkaTemplate
	) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
			kafkaTemplate,
			(record, exception) -> {
				String dltTopic = record.topic() + KafkaTopicType.Topics.DLT_TOPIC_SUFFIX;
				return new TopicPartition(dltTopic, record.partition());
			}
		);

		//기존 헤더도 포함
		recoverer.setAppendOriginalHeaders(true);

		//커스텀 헤더 설정
		recoverer.setHeadersFunction((record, exception) -> {
			RecordHeaders headers = new RecordHeaders();

			headers.add("original_topic", record.topic().getBytes(StandardCharsets.UTF_8));

			String exceptionType = exception.getCause() != null
				? exception.getCause().getClass().getSimpleName()
				: exception.getClass().getSimpleName();
			headers.add("exception_type", exceptionType.getBytes(StandardCharsets.UTF_8));

			String exceptionMessage = exception.getCause() != null
				? exception.getCause().getMessage()
				: exception.getMessage();
			if (exceptionMessage != null) {
				headers.add("exception_message", exceptionMessage.getBytes(StandardCharsets.UTF_8));
			}

			String timestampStr = Instant.ofEpochMilli(record.timestamp()).toString();
			headers.add("timestamp_readable", timestampStr.getBytes(StandardCharsets.UTF_8));

			return headers;
		});

		recoverer.setFailIfSendResultIsError(false);

		return recoverer;
	}

	@Bean
	public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
		FixedBackOff backOff = new FixedBackOff(
			KafkaConsumerConstants.RETRY_DELAY_MS,
			KafkaConsumerConstants.MAX_ATTEMPT_COUNT - 1
		);

		DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

		errorHandler.addNotRetryableExceptions(
			DeserializationException.class,
			MessageConversionException.class
		);

		errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
			log.warn("메시지 처리 재시도: topic={}, attempt={}, error={}",
				record.topic(), deliveryAttempt, ex.getMessage());
		});

		return errorHandler;
	}

	// ============================================================
	// 내부 범용 Consumer - 헤더타입 존재하는 경우
	// ============================================================

	@Bean
	public ConsumerFactory<Object, Object> consumerFactory() {
		Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties());

		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());

		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

		config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);

		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<Object, Object>
	kafkaListenerContainerFactory(
		DefaultErrorHandler errorHandler,
		RecordMessageConverter dltMessageConverter) {
		ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
			new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory());
		factory.setCommonErrorHandler(errorHandler);
		factory.setRecordMessageConverter(dltMessageConverter);

		KafkaProperties.Listener listenerProperties = kafkaProperties.getListener();
		factory.setConcurrency(listenerProperties.getConcurrency());
		factory.getContainerProperties().setAckMode(listenerProperties.getAckMode());

		return factory;
	}

	// ============================================================
	// CrawledProductMessage 전용
	// ============================================================

	@Bean
	public ConsumerFactory<String, CrawledProductMessage> crawledProductConsumerFactory() {
		return createConsumerFactory(CrawledProductMessage.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CrawledProductMessage>
	crawledProductKafkaListenerContainerFactory(
		DefaultErrorHandler errorHandler,
		RecordMessageConverter dltMessageConverter) {
		return createListenerContainerFactory(crawledProductConsumerFactory(), errorHandler, dltMessageConverter);
	}

	// ============================================================
	// CrawledReviewMessage 전용
	// ============================================================

	@Bean
	public ConsumerFactory<String, CrawledReviewMessage> crawledReviewConsumerFactory() {
		return createConsumerFactory(CrawledReviewMessage.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CrawledReviewMessage>
	crawledReviewKafkaListenerContainerFactory(
		DefaultErrorHandler errorHandler,
		RecordMessageConverter dltMessageConverter) {
		return createListenerContainerFactory(crawledReviewConsumerFactory(), errorHandler, dltMessageConverter);
	}

	// ============================================================
	// Producer
	// ============================================================

	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> config = new HashMap<>(kafkaProperties.buildProducerProperties());

		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	// ============================================================
	// 외부용 consumerFactory - 헤더 타입 없는 경우
	// ============================================================

	private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> messageType) {
		Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties());

		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, messageType.getName());
		config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

		return new DefaultKafkaConsumerFactory<>(config);
	}

	private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerContainerFactory(
		ConsumerFactory<String, T> consumerFactory,
		DefaultErrorHandler errorHandler,
		RecordMessageConverter dltMessageConverter
	) {
		ConcurrentKafkaListenerContainerFactory<String, T> factory =
			new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);
		factory.setRecordMessageConverter(dltMessageConverter);

		KafkaProperties.Listener listenerProperties = kafkaProperties.getListener();
		factory.setConcurrency(listenerProperties.getConcurrency());
		factory.getContainerProperties().setAckMode(listenerProperties.getAckMode());

		return factory;
	}
}