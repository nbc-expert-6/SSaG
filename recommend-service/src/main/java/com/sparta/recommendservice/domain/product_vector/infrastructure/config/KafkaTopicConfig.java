package com.sparta.recommendservice.domain.product_vector.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic embeddingUpdatedTopic() {
		return new NewTopic("embedding-updated", 3, (short)1);
	}

	@Bean
	public NewTopic recommendCompletedTopic() {
		return new NewTopic("recommend-completed", 3, (short)1);
	}

}
