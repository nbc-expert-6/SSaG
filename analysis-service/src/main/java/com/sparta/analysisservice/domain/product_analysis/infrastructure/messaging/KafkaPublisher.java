package com.sparta.analysisservice.domain.product_analysis.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.analysisservice.domain.product_analysis.infrastructure.dto.ClickEvent;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackPublishClickEvent")
	public void publishClickEvent(UUID sessionId, UUID productId, LocalDateTime clickedAt) {
		ClickEvent event = new ClickEvent(sessionId, productId, clickedAt);
		kafkaTemplate.send("click.event", event);
		log.info("[PUBLISH → click.event] sessionId={}, productId={}, clickedAt={}",
			sessionId, productId, clickedAt);
	}

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackPublishClickEvent")
	public void publishProductAnalysisEvent(UUID sessionId, UUID productId, LocalDateTime clickedAt) throws
		JsonProcessingException {
		ClickEvent event = new ClickEvent(sessionId, productId, clickedAt);

		String data = objectMapper.writeValueAsString(event);
		kafkaTemplate.send("product.analysis", data);
		log.info("[PUBLISH → product.analysis] sessionId={}, productId={}, clickedAt={}",
			sessionId, productId, clickedAt);
	}

	public void fallbackPublishClickEvent(UUID sessionId, UUID productId, LocalDateTime clickedAt, Throwable e) {
		log.error("Kafka click.event publish failed -> sessionId={}, productId={}, error={}",
			sessionId, productId, e.getMessage(), e);
		// 모니터링, dead-letter 큐 적재 등 필요 시 구현
	}

	public void fallbackPublishProductAnalysisEvent(UUID sessionId, UUID productId, LocalDateTime clickedAt,
		Throwable e) {
		log.error("[Fallback] product.analysis publish failed -> sessionId={}, productId={}, error={}",
			sessionId, productId, e.getMessage(), e);
		// 모니터링, dead-letter 큐 적재 등 필요 시 구현
	}

}
