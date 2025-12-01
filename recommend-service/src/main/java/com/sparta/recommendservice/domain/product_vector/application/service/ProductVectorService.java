package com.sparta.recommendservice.domain.product_vector.application.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.recommendservice.domain.product_vector.application.service.dto.ProductInfoDto;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;
import com.sparta.recommendservice.domain.product_vector.domain.repository.ProductVectorRepository;
import com.sparta.recommendservice.domain.product_vector.infrastructure.dto.ProductVectorDto;
import com.sparta.recommendservice.domain.product_vector.infrastructure.dto.ScoredProduct;
import com.sparta.recommendservice.domain.product_vector.infrastructure.messaging.KafkaPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVectorService {

	private final PythonRunner pythonRunner;
	private final ProductVectorRepository repository;
	private final KafkaPublisher kafkaPublisher;
	private final ProductClient productClient;
	private final RedisTemplate<String, Object> redisTemplate;

	private static final String LAST_UPDATE_KEY = "product_vector:last_update";

	// 가중치 상수
	private static final double W_EMBEDDING = 0.6;
	private static final double W_PRICE = 0.2;
	private static final double W_BRAND = 0.1;
	private static final double W_CATEGORY = 0.1;
	private static final int BATCH_SIZE = 500;

	// 매일 자정에 벡터 업데이트 실행
	@Scheduled(cron = "0 0 0 * * *")
	public void updateProductVectors() {
		try {
			pythonRunner.run();

			LocalDateTime lastUpdate = getLastUpdateTime();
			List<UUID> updatedProductIds = repository.findUpdatedProductIdsSince(lastUpdate);

			if (updatedProductIds.isEmpty()) {
				log.info("변경된 상품 없음, 처리 생략");
				return;
			}

			int batchSize = BATCH_SIZE;
			for (int i = 0; i < updatedProductIds.size(); i += batchSize) {
				List<UUID> batch =
					updatedProductIds.subList(i, Math.min(i + batchSize, updatedProductIds.size()));
				kafkaPublisher.publishEmbeddingUpdated(batch);
			}

			saveLastUpdateTime(LocalDateTime.now());

		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Vector update failed!", e);
		}
	}

	private LocalDateTime getLastUpdateTime() {
		String timeStr = (String)redisTemplate.opsForValue().get(LAST_UPDATE_KEY);
		if (timeStr == null || timeStr.isBlank()) {
			return LocalDateTime.of(0001, 1, 1, 0, 0);
		}
		return LocalDateTime.parse(timeStr);
	}

	private void saveLastUpdateTime(LocalDateTime time) {
		String value = time.toString();
		redisTemplate.opsForValue().set(LAST_UPDATE_KEY, value);
	}

	// 벡터 내적 계산
	private double cosineDistance(float[] vec1, float[] vec2) {

		if (vec1.length != vec2.length) {
			throw new IllegalStateException("Vector lengths do not match.");
		}

		double dot = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vec1.length; i++) {
			dot += vec1[i] * vec2[i];
			normA += vec1[i] * vec1[i];
			normB += vec2[i] * vec2[i];
		}

		if (normA == 0.0 || normB == 0.0) {
			return 0.0; // 완전 무관 → 거리 1.0
		}

		return 1.0 - (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
	}

	// Embedding + metadata weighted score hybrid Recommendation
	public List<UUID> recommend(ProductVector target, int candidateSize, int finalTopK) throws JsonProcessingException {
		double wEmbedding = W_EMBEDDING;
		double wPrice = W_PRICE;
		double wBrand = W_BRAND;
		double wCategory = W_CATEGORY;

		// 1. HNSW 기반 벡터 유사도로 1차 후보군 조회
		List<ProductVectorDto> candidates =
			repository.findTopKByEmbedding(target.getEmbedding(), candidateSize);

		float[] targetVec = target.getEmbedding().toArray();

		ProductInfoDto targetInfo = productClient.getProductInfo(target.getProductId());
		//ProductInfoDto targetInfo = getDummyProductInfo(target.getProductId());

		// 2. 후보 상품 각각에 점수를 계산하고 정렬 후 상위 N개 선정
		List<ScoredProduct> scoredProducts = candidates.stream()
			// 기준 상품과 동일한 상품은 제외
			.filter(p -> !p.getProductId().equals(target.getProductId()))
			// 각 상품에 대해 점수 계산
			.map(p -> {

				ProductInfoDto candidateInfo = productClient.getProductInfo(p.getProductId());
				//ProductInfoDto candidateInfo = getDummyProductInfo(p.getProductId());

				double score = 0.0;

				double embeddingScore = 1.0 - cosineDistance(targetVec, p.getEmbedding());
				score += embeddingScore * wEmbedding;

				// 브랜드 일치 시 가중치 추가
				if (Objects.equals(candidateInfo.brand(), targetInfo.brand())) {
					score += wBrand;
				}

				// 카테고리 일치 시 가중치 추가
				if (Objects.equals(candidateInfo.categoryMediumId(), targetInfo.categoryMediumId())) {
					score += wCategory;
				}

				// 가격 유사도 계산 (차이가 적을수록 점수 높음)
				BigDecimal targetPrice = targetInfo.price();
				BigDecimal candidatePrice = candidateInfo.price();

				if (targetPrice != null && candidatePrice != null &&
					targetPrice.compareTo(BigDecimal.ZERO) > 0) {
					double priceDiff = candidatePrice.subtract(targetPrice).abs().doubleValue();
					double priceScore = 1.0 - priceDiff / targetPrice.doubleValue();
					score += priceScore * wPrice;
				}

				return new ScoredProduct(p.getProductId(), score);
			})
			// 점수 기준 내림차순 정렬
			.sorted(Comparator.comparingDouble(ScoredProduct::getScore).reversed())
			.limit(finalTopK) // 최종 Top-K만 가져오기
			.toList();

		return scoredProducts.stream().map(ScoredProduct::getProductId).toList();
	}

	// 임시 Stub DTO 생성 메서드
	// TODO : FeignClient 정상 확인되면 지우기
	private ProductInfoDto getDummyProductInfo(UUID productId) {
		return new ProductInfoDto(
			productId,
			"dummyBrand",
			UUID.randomUUID(),
			BigDecimal.valueOf(1000)
		);
	}

}
