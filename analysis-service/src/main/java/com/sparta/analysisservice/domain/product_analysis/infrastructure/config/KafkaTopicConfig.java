package com.sparta.analysisservice.domain.product_analysis.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic clickEventTopic() {
		return new NewTopic("click.event", 3, (short)1);
	}

}
