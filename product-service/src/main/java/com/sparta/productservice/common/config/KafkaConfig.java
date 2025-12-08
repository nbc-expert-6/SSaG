package com.sparta.productservice.common.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
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

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

	private final KafkaProperties kafkaProperties;

	/**
	 * Kafka Consumer Factory 생성
	 *
	 * application.yml의 설정을 기반으로 ConsumerFactory를 구성합니다.
	 * Key와 Value 모두 JSON으로 역직렬화합니다.
	 *
	 * @return ConsumerFactory
	 */
	@Bean
	public ConsumerFactory<Object, Object> consumerFactory() {
		Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties());

		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
			org.apache.kafka.common.serialization.StringDeserializer.class.getName());

		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

		config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

		config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
		config.remove(JsonDeserializer.VALUE_DEFAULT_TYPE);

		return new DefaultKafkaConsumerFactory<>(config);
	}

	/**
	 * Kafka Listener Container Factory 설정
	 *
	 * application.yml의 listener 설정을 사용합니다:
	 *
	 *   ack-mode: manual
	 *   concurrency: concurrency-thread
	 *
	 * @return ConcurrentKafkaListenerContainerFactory
	 */
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

	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> config = new HashMap<>(kafkaProperties.buildProducerProperties());

		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
			org.apache.kafka.common.serialization.StringSerializer.class);

		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
			org.springframework.kafka.support.serializer.JsonSerializer.class);

		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}
