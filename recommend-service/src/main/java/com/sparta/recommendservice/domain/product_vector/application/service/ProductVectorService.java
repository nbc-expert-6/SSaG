package com.sparta.recommendservice.domain.product_vector.application.service;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;
import com.sparta.recommendservice.domain.product_vector.domain.repository.ProductVectorRepository;
import com.sparta.recommendservice.domain.product_vector.infrastructure.dto.ProductVectorDto;
import com.sparta.recommendservice.domain.product_vector.infrastructure.dto.ScoredProduct;
import com.sparta.recommendservice.domain.product_vector.infrastructure.messaging.KafkaPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVectorService {

	private final PythonRunner pythonRunner;
	private final ProductVectorRepository repository;
	private final KafkaPublisher kafkaPublisher;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// ✅ 가중치 상수
	private static final double W_EMBEDDING = 0.6;
	private static final double W_PRICE = 0.2;
	private static final double W_BRAND = 0.1;
	private static final double W_CATEGORY = 0.1;

	// 매일 자정에 벡터 업데이트 실행
	@Scheduled(cron = "0 0 0 * * *")
	public void updateProductVectors() {
		try {
			pythonRunner.run();

			List<UUID> vectors = repository.findAllProductId();

			for (UUID productId : vectors) {
				kafkaPublisher.publishEmbeddingUpdated(productId);
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Vector update failed!", e);
		}
	}

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

		// 2. 후보 상품 각각에 점수를 계산하고 정렬 후 상위 N개 선정
		List<ScoredProduct> scoredProducts = candidates.stream()
			// 기준 상품과 동일한 상품은 제외
			.filter(p -> !p.getProductId().equals(target.getProductId()))
			// 각 상품에 대해 점수 계산
			.map(p -> {
				Map<String, Object> meta = toMap(p.getMetadata());
				Map<String, Object> targetMeta = toMap(target.getMetadata());

				double score = 0.0;

				double embeddingScore = 1.0 - cosineDistance(targetVec, p.getEmbedding());
				score += embeddingScore * wEmbedding;

				// 브랜드 일치 시 가중치 추가
				if (Objects.equals(meta.get("brand"), targetMeta.get("brand"))) {
					score += wBrand;
				}

				// 카테고리 일치 시 가중치 추가
				if (Objects.equals(meta.get("categoryId"), targetMeta.get("categoryId"))) {
					score += wCategory;
				}

				// 가격 유사도 계산 (차이가 적을수록 점수 높음)
				Double targetPrice = targetMeta.get("price") != null ?
					Double.parseDouble(targetMeta.get("price").toString()) : null;

				Double price = meta.get("price") != null ?
					Double.parseDouble(meta.get("price").toString()) : null;

				if (targetPrice != null && price != null && targetPrice > 0) {
					double priceDiff = Math.abs(price - targetPrice);
					double priceScore = 1.0 - priceDiff / targetPrice;
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

	private Map<String, Object> toMap(JsonNode node) {
		if (node == null)
			return Collections.emptyMap();
		return new ObjectMapper().convertValue(node, new TypeReference<>() {
		});
	}

}
