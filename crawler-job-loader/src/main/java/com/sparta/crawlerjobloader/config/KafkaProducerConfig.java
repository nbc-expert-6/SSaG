package com.sparta.crawlerjobloader.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

	private final KafkaProperties kafkaProperties;

	public KafkaProducerConfig(KafkaProperties kafkaProperties) {
		this.kafkaProperties = kafkaProperties;
	}

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}