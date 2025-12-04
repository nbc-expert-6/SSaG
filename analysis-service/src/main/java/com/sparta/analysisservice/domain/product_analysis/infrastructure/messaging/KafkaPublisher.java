package com.sparta.analysisservice.domain.product_analysis.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.analysisservice.domain.product_analysis.infrastructure.dto.UserActivityEvent;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackpublishUserEvent")
	public void publishUserEvent(UUID sessionId, UUID productId, String eventType, String meta) {
		UserActivityEvent event = UserActivityEvent.of(sessionId, productId, eventType, meta);

		kafkaTemplate.send("user.event", event);
		log.info("[PUBLISH → user.event] sessionId={}, eventType={}, productId={}, meta={}",
			sessionId, eventType, productId, meta);
	}

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackPublishProductAnalysisEvent")
	public void publishProductAnalysisEvent(UUID sessionId, UUID productId, Instant timestamp) throws
		JsonProcessingException {
		UserActivityEvent event = new UserActivityEvent(sessionId, productId, "CLICK", timestamp, null);

		String data = objectMapper.writeValueAsString(event);
		kafkaTemplate.send("product.analysis", data);
		log.info("[PUBLISH → product.analysis] sessionId={}, productId={}, timestamp={}",
			sessionId, productId, timestamp);
	}

	public void fallbackpublishUserEvent(UUID sessionId, UUID productId, Instant timestamp, Throwable e) {
		log.error("Kafka user.event publish failed -> sessionId={}, productId={}, error={}",
			sessionId, productId, e.getMessage(), e);
		// 모니터링, dead-letter 큐 적재 등 필요 시 구현
	}

	public void fallbackPublishProductAnalysisEvent(UUID sessionId, UUID productId, Instant timestamp,
		Throwable e) {
		log.error("[Fallback] product.analysis publish failed -> sessionId={}, productId={}, error={}",
			sessionId, productId, e.getMessage(), e);
		// 모니터링, dead-letter 큐 적재 등 필요 시 구현
	}

}
