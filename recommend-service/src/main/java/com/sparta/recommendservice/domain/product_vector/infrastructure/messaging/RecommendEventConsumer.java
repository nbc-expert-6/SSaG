package com.sparta.recommendservice.domain.product_vector.infrastructure.messaging;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.recommendservice.domain.product_vector.application.service.ProductVectorService;
import com.sparta.recommendservice.domain.product_vector.application.service.RecommendCacheService;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;
import com.sparta.recommendservice.domain.product_vector.domain.event.EmbeddingUpdatedEvent;
import com.sparta.recommendservice.domain.product_vector.domain.repository.ProductVectorRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendEventConsumer {

	private final ProductVectorRepository repository;
	private final KafkaPublisher kafkaPublisher;
	private final ProductVectorService productVectorService;
	private final RecommendCacheService recommendCacheService;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "embedding.updated")
	public void handleEmbeddingUpdated(String message) {
		EmbeddingUpdatedEvent event;
		try {
			// 메시지 변환
			event = objectMapper.readValue(message, EmbeddingUpdatedEvent.class);
		} catch (JsonProcessingException e) {
			log.error("[KafkaListener] 메시지 변환 실패 -> message={}", message, e);
			return; // 변환 실패 시 건너뜀
		}

		List<UUID> productIds = event.productIds();
		log.info("배치 이벤트 수신 -> size={}", productIds.size());

		for (UUID productId : productIds) {
			try {
				// CircuitBreaker 적용
				processRecommendation(productId);
			} catch (Exception e) {
				log.error("[KafkaListener] 추천 처리 실패 -> productId={}", productId, e);
				// fallback 큐 적재나 알림 처리 가능
			}
		}
	}

	@CircuitBreaker(name = "recommendationCB", fallbackMethod = "fallbackRecommendation")
	public void processRecommendation(UUID productId) throws JsonProcessingException {
		ProductVector target = repository.findByProductId(productId)
			.orElseThrow(() -> new NoSuchElementException("No such vector found"));

		log.info("[Recommendation] 추천 계산 시작 -> productId={}", productId);
		List<UUID> recommended = productVectorService.recommend(target, 30, 10);

		recommendCacheService.saveRecommend(productId, recommended);
		log.info("[Redis] 추천 결과 저장 완료 -> key=recommend:{}, value={}", productId, recommended);

		kafkaPublisher.publishRecommendCompleted(productId, recommended);
		log.info("[Kafka] recommend.completed 이벤트 발행 -> productId={}, recommended={}", productId, recommended);
	}

	public void fallbackEmbeddingUpdated(EmbeddingUpdatedEvent event, Throwable e) {
		List<UUID> productIds = event.productIds();
		log.error("[Fallback] 추천 계산 또는 Redis 저장 실패, productIds={}, exception={}", productIds, e);
		// 재시도 큐 적재, 모니터링, 알림 등
	}

}
