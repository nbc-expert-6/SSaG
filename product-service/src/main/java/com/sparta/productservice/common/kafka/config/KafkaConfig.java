package com.sparta.productservice.common.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
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
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;
import com.sparta.productservice.review.infra.event.message.CrawledReviewMessage;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

	private final KafkaProperties kafkaProperties;

	// ============================================================
	// 내부 범용 Consumer - 헤더타입 존재
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
	kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
			new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory());

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
		Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties());

		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CrawledProductMessage.class.getName());
		config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CrawledProductMessage>
	crawledProductKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, CrawledProductMessage> factory =
			new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(crawledProductConsumerFactory());

		KafkaProperties.Listener listenerProperties = kafkaProperties.getListener();
		factory.setConcurrency(listenerProperties.getConcurrency());
		factory.getContainerProperties().setAckMode(listenerProperties.getAckMode());

		return factory;
	}

	// ============================================================
	// CrawledReviewMessage 전용
	// ============================================================

	@Bean
	public ConsumerFactory<String, CrawledReviewMessage> crawledReviewConsumerFactory() {
		Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties());

		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CrawledReviewMessage.class.getName());
		config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CrawledReviewMessage>
	crawledReviewKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, CrawledReviewMessage> factory =
			new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(crawledReviewConsumerFactory());

		KafkaProperties.Listener listenerProperties = kafkaProperties.getListener();
		factory.setConcurrency(listenerProperties.getConcurrency());
		factory.getContainerProperties().setAckMode(listenerProperties.getAckMode());

		return factory;
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
}