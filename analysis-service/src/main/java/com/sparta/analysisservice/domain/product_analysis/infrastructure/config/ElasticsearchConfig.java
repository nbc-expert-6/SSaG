package com.sparta.analysisservice.domain.product_analysis.infrastructure.config;

import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig {

	private final RestClient restClient;

	@Bean
	public ObjectMapper elasticsearchObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}

	@Bean
	public ElasticsearchClient elasticsearchClient(RestClient restClient, ObjectMapper elasticsearchObjectMapper) {
		JacksonJsonpMapper mapper = new JacksonJsonpMapper(elasticsearchObjectMapper);
		ElasticsearchTransport transport = new RestClientTransport(restClient, mapper);
		return new ElasticsearchClient(transport);
	}

}
