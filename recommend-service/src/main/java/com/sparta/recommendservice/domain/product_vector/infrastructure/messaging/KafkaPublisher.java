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
			kafkaTemplate.send("embedding.updated", message);
			log.info("Kafka 배치 메시지 전송 성공 -> size={}", productIds.size());
		} catch (JsonProcessingException e) {
			log.error("Kafka 직렬화 실패", e);
		}
	}

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

	// Fallback Method
	public void fallbackPublishEmbeddingUpdated(List<UUID> productIds, Throwable e) {
		log.error("Kafka embedding.updated publish failed -> size={}, error={}",
			productIds != null ? productIds.size() : 0, e.getMessage(), e);
		// 알림, 모니터링, 재시도 큐 적재
	}

	public void fallbackPublishRecommendCompleted(UUID productId, List<UUID> recommendedIds, Throwable e) {
		log.error("Kafka recommend.completed publish failed : {}", productId, e);
	}

}
