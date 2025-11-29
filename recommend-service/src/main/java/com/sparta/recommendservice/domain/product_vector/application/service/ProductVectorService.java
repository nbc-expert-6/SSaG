package com.sparta.recommendservice.domain.product_vector.application.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;
import com.sparta.recommendservice.domain.product_vector.domain.repository.ProductVectorRepository;
import com.sparta.recommendservice.domain.product_vector.infrastructure.dto.ScoredProduct;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVectorService {

	private final PythonRunner pythonRunner;
	private final ProductVectorRepository repository;

	public void updateProductVectors() {
		try {
			pythonRunner.run();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Vector update failed!", e);
		}
	}

	// Embedding + metadata weighted score hybrid Recommendation
	public List<UUID> recommend(ProductVector target, int candidateSize, int finalTopK) {
		double wEmbedding = 0.6;
		double wPrice = 0.2;
		double wBrand = 0.1;
		double wCategory = 0.1;

		// 1. HNSW 기반 벡터 유사도로 1차 후보군 조회
		List<ProductVector> candidates = repository.findTopKByEmbedding(target.getEmbedding(), candidateSize);

		// 2. 후보 상품 각각에 점수를 계산하고 정렬 후 상위 N개 선정
		List<ScoredProduct> scoredProducts = candidates.stream()
			// 기준 상품과 동일한 상품은 제외
			.filter(p -> !p.getProductId().equals(target.getProductId()))
			// 각 상품에 대해 점수 계산
			.map(p -> {
				Map<String, Object> meta = parseMetadata(p.getMetadata());
				Map<String, Object> targetMeta = parseMetadata(target.getMetadata());

				double score = 0.0;

				double embeddingScore = 1.0 - target.cosineDistance(p);
				score += embeddingScore * wEmbedding;

				// 브랜드 일치 시 가중치 추가
				if (meta.get("brand").equals(targetMeta.get("brand"))) {
					score += wBrand;
				}

				// 카테고리 일치 시 가중치 추가
				if (meta.get("categoryId").equals(targetMeta.get("categoryId"))) {
					score += wCategory;
				}

				// 가격 유사도 계산 (차이가 적을수록 점수 높음)
				double priceDiff = Math.abs(Double.parseDouble(meta.get("price").toString()) -
					Double.parseDouble(targetMeta.get("price").toString()));

				double priceScore = 1.0 - priceDiff / Double.parseDouble(targetMeta.get("price").toString());

				score += priceScore * wPrice;

				return new ScoredProduct(p.getProductId(), score);
			})
			// 점수 기준 내림차순 정렬
			.sorted(Comparator.comparingDouble(ScoredProduct::getScore).reversed())
			.limit(finalTopK) // 최종 Top-K만 가져오기
			.toList();

		return scoredProducts.stream().map(ScoredProduct::getProductId).toList();
	}

	// JSON 형태의 metadata 문자열을 Map 타입으로 변환
	private Map<String, Object> parseMetadata(String metadatJson) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(metadatJson, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse metadata", e);
		}
	}
}
