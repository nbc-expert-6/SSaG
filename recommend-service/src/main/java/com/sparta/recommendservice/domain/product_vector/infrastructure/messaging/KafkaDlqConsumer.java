package com.sparta.recommendservice.domain.product_vector.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.recommendservice.domain.product_vector.domain.event.EmbeddingUpdatedEvent;
import com.sparta.recommendservice.domain.product_vector.domain.event.RecommendCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaDlqConsumer {

	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "embedding.updated.dlq", groupId = "dlq-embedding-updated-group")
	public void listendEmbeddingUpdatedDlq(String payload) {
		EmbeddingUpdatedEvent event = deserialize(payload);
		if (event != null) {
			log.error("[DLQ 수신 - embedding.updated.dlq] 비정상 처리된 이벤트 감지 -> {}", event);
		}
	}

	@KafkaListener(topics = "recommend.completed.dlq", groupId = "dlq-recommend-completed-group")
	public void listenRecommendCompletedDlq(String payload) {
		RecommendCompletedEvent event = deserializeRecommendCompletedEvent(payload);
		if (event != null) {
			log.error("[DLQ 수신 - recommend.completed.dlq] 추천 이벤트 비정상 처리 -> productId={}, recommendedIds={}",
				event.productId(), event.recommendedIds());
		}
	}

	private EmbeddingUpdatedEvent deserialize(String payload) {
		try {
			return objectMapper.readValue(payload, EmbeddingUpdatedEvent.class);
		} catch (JsonProcessingException e) {
			log.error("DLQ 메시지 역직렬화 실패 → payload={}", payload, e);
			return null;
		}
	}

	private RecommendCompletedEvent deserializeRecommendCompletedEvent(String payload) {
		try {
			return objectMapper.readValue(payload, RecommendCompletedEvent.class);
		} catch (Exception e) {
			log.error("DLQ 메시지 역직렬화 실패 → payload={}", payload, e);
			return null;
		}
	}
}
