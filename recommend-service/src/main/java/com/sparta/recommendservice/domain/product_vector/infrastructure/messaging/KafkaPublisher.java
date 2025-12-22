package com.sparta.recommendservice.domain.product_vector.infrastructure.messaging;

import java.util.List;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.recommendservice.domain.product_vector.domain.event.EmbeddingUpdatedEvent;
import com.sparta.recommendservice.domain.product_vector.domain.event.RecommendCompletedEvent;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackPublishEmbeddingUpdated")
	public void publishEmbeddingUpdated(List<UUID> productIds) {
		try {
			String message = objectMapper.writeValueAsString(new EmbeddingUpdatedEvent(productIds));
			kafkaTemplate.send("embedding-updated", message);
			log.info("Kafka 배치 메시지 전송 성공 -> size={}", productIds.size());
		} catch (JsonProcessingException e) {
			log.error("Kafka 직렬화 실패", e);
		}
	}

	public void fallbackPublishEmbeddingUpdated(List<UUID> productIds, Throwable e) {
		log.error("Kafka embedding-updated publish failed -> size={}, error={}",
			productIds != null ? productIds.size() : 0, e.getMessage(), e);

		List<UUID> defaultIds = List.of();

		try {
			String defaultMessage = objectMapper.writeValueAsString(new EmbeddingUpdatedEvent(defaultIds));
			kafkaTemplate.send("embedding-updated", defaultMessage);
			log.info("Fallback: 디폴트 Kafka 메시지 전송 완료 -> size={}", defaultIds.size());
		} catch (JsonProcessingException ex) {
			log.error("Fallback Kafka 직렬화 실패", ex);
		}

		try {
			String dlqMessage = objectMapper.writeValueAsString(new EmbeddingUpdatedEvent(productIds));
			publishToDlq("embedding-updated-dlq", dlqMessage);

		} catch (JsonProcessingException ex) {
			log.error("DLQ Kafka 직렬화 실패 -> size={}, error={}",
				productIds != null ? productIds.size() : 0, ex.getMessage(), ex);
		}
	}

	// ======================================================================================

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackPublishRecommendCompleted")
	public void publishRecommendCompleted(UUID productId, List<UUID> recommendedIds) {
		try {
			String message = objectMapper.writeValueAsString(new RecommendCompletedEvent(productId, recommendedIds));
			kafkaTemplate.send("recommend.completed", message);
			log.info("Kafka 메시지 전송 성공: recommend.completed -> productId={}, recommendedIds={}", productId,
				recommendedIds);
		} catch (JsonProcessingException e) {
			log.error("Kafka recommend.completed 직렬화 실패: {}", productId, e);
		}
	}

	public void fallbackPublishRecommendCompleted(UUID productId, List<UUID> recommendedIds, Throwable e) {
		log.error("Kafka recommend.completed publish failed : {}", productId, e);

		try {
			String dlqMessage = objectMapper.writeValueAsString(new RecommendCompletedEvent(productId, recommendedIds));
			publishToDlq("recommend.completed.dlq", dlqMessage);

		} catch (JsonProcessingException ex) {
			log.error("DLQ Kafka 직렬화 실패 -> productId={}, error={}", productId, ex.getMessage(), ex);
		}
	}

	public void publishToDlq(String topic, String message) {
		try {
			kafkaTemplate.send(topic, message);
			log.info("DLQ 메시지 전송 완료 -> topic={}, message={}", topic, message);
		} catch (Exception e) {
			log.error("DLQ 메시지 전송 실패 -> topic={}, message={}", topic, message, e);
		}
	}

}
