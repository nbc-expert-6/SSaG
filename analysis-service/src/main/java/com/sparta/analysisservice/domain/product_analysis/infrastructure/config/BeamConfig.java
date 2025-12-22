package com.sparta.analysisservice.domain.product_analysis.infrastructure.config;

import org.apache.beam.sdk.io.elasticsearch.ElasticsearchIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeamConfig {

	@Bean
	public PipelineOptions pipelineOptions() {
		return PipelineOptionsFactory.create();
	}

	@Bean
	public ElasticsearchIO.ConnectionConfiguration configuration() {
		return ElasticsearchIO.ConnectionConfiguration.create(
				new String[] {System.getenv("ES_HOST")},
				"user_events"
			)
			.withUsername(System.getenv("ES_USERNAME"))
			.withPassword(System.getenv("ES_PASSWORD"));
	}

}
