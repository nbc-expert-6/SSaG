package com.sparta.analysisservice.domain.product_analysis.infrastructure.config;

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

}
