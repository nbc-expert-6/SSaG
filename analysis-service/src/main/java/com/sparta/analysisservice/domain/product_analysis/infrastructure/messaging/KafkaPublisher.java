package com.sparta.analysisservice.domain.product_analysis.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.analysisservice.domain.product_analysis.application.service.SessionClusterService;
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
	private final CircuitBreakerFactory circuitBreakerFactory;
	private final SessionClusterService sessionClusterService;

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackpublishUserEvent")
	public void publishUserEvent(UUID sessionId, UUID productId, String eventType, String meta) {

		UserActivityEvent event = UserActivityEvent.of(sessionId, productId, eventType, meta);

		try {
			String jsonPayload = objectMapper.writeValueAsString(event);
			kafkaTemplate.send("user-event", jsonPayload);
			log.info("[PUBLISH → user-event] sessionId={}, eventType={}, productId={}, meta={}",
				sessionId, eventType, productId, meta);
		} catch (JsonProcessingException e) {
			log.error("Kafka 전송 실패 - JSON 직렬화 오류 sessionId={}, productId={}, error={}",
				sessionId, productId, e.getMessage(), e);
		}
	}

	public void fallbackpublishUserEvent(UUID sessionId, UUID productId, String eventType, String meta, Throwable e) {
		log.error("Kafka user-event publish failed -> sessionId={}, productId={}, error={}",
			sessionId, productId, e.getMessage(), e);

		// default 이벤트 객체
		UserActivityEvent defaultEvent = UserActivityEvent.of(
			sessionId != null ? sessionId : UUID.randomUUID(),
			productId != null ? productId : UUID.fromString("00000000-0000-0000-0000-000000000000"),
			eventType != null ? eventType : "UNKNOWN_EVENT",
			meta != null ? meta : "DEFAULT_META"
		);

		// 1. 본 Topic으로 defaultEvent라도 무조건 재전송 (중단 방지 운영 목적)
		try {
			kafkaTemplate.send("user-event", defaultEvent);
			log.warn("Fallback 기본 이벤트 재전송 완료 → user-event");
		} catch (Exception ex) {
			log.error("Fallback 본 Topic(user-event) 재전송 실패 — DLQ 처리로 전환 (sessionId={}, productId={}, eventType={})",
				sessionId, productId, eventType, ex);
		}

		var dlqBreaker = circuitBreakerFactory.create("kafkaDlqBreaker");

		dlqBreaker.run(
			() -> {
				String jsonPayload = null;
				try {
					jsonPayload = objectMapper.writeValueAsString(
						UserActivityEvent.of(sessionId, productId, eventType, meta)
					);
				} catch (JsonProcessingException ex) {
					log.error("DLQ 전송 실패 - JSON 직렬화 오류", e);
				}
				kafkaTemplate.send("user-event-dlq", jsonPayload);
				log.warn("DLQ 전송 완료 → user-event-dlq");
				return null;
			}, throwable -> {
				log.error("DLQ 전송도 실패 -> 회로 OPEN 가능성 있음 (Slack/문자 알림 필요)");
				return null;
			}
		);

		log.warn("DLQ로 이동 완료 => user-event-dlq");
	}

	//============================================================================================

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackPublishProductAnalysisEvent")
	public void publishProductAnalysisEvent(UUID sessionId, UUID productId, Instant timestamp) {

		if (sessionClusterService.isAnomalousSession(sessionId)) {
			log.info(
				"[ANOMALY FILTER] sessionId={}, skipped recommendation event",
				sessionId
			);

			return;
		}

		UserActivityEvent event = new UserActivityEvent(sessionId, productId, "CLICK", timestamp, null);

		try {
			String data = objectMapper.writeValueAsString(event);
			kafkaTemplate.send("product-analysis", data);
			log.info("[PUBLISH → product-analysis] sessionId={}, productId={}, timestamp={}",
				sessionId, productId, timestamp);
		} catch (JsonProcessingException e) {
			log.error("Kafka 전송 실패 - JSON 직렬화 오류 sessionId={}, productId={}, timestamp={}, error={}",
				sessionId, productId, timestamp, e.getMessage(), e);
		}
	}

	public void fallbackPublishProductAnalysisEvent(UUID sessionId, UUID productId, Instant timestamp,
		Throwable e) {
		log.error("[Fallback] product-analysis publish failed -> sessionId={}, productId={}, error={}",
			sessionId, productId, e.getMessage(), e);

		UserActivityEvent defaultEvent = new UserActivityEvent(
			sessionId != null ? sessionId : UUID.randomUUID(),
			productId != null ? productId : UUID.fromString("00000000-0000-0000-0000-000000000000"),
			"UNKNOWN_EVENT",
			timestamp != null ? timestamp : Instant.now(),
			"DEFAULT_META"
		);

		try {
			String defaultData = objectMapper.writeValueAsString(defaultEvent);
			kafkaTemplate.send("product-analysis", defaultData);
			log.warn("Fallback 기본 이벤트 재전송 완료 → product-analysis");
		} catch (Exception ex) {
			log.error(
				"Fallback 본 Topic(product-analysis) 재전송 실패 — DLQ로 대체 처리 진행 (sessionId={}, productId={}, timestamp={})",
				sessionId, productId, timestamp, ex);
		}

		try {
			String data = objectMapper.writeValueAsString(
				new UserActivityEvent(sessionId, productId, "UNKNOWN_EVENT", timestamp, null)
			);
			kafkaTemplate.send("product-analysis-dlq", data);
			log.warn("DLQ 저장 완료 → product-analysis-dlq");
		} catch (JsonProcessingException ex) {
			log.error("DLQ 메시지 직렬화 실패 → 운영자 알림 필요 | {}", ex.getMessage(), ex);
		} catch (Exception ex) {
			log.error("DLQ 전송조차 실패 → 운영자 알림 필요 | {}", ex.getMessage(), ex);
		}

	}

}
