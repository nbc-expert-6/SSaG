package com.sparta.recommendservice.domain.product_vector.domain.entity;

import java.util.UUID;

import com.pgvector.PGvector;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_product_vector")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVector {

	@Id
	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "embedding", columnDefinition = "public.vector(128)", nullable = false)
	private PGvector embedding;

	@Column(name = "metadta", columnDefinition = "jsonb")
	private String metadata;

	@Builder
	public ProductVector(UUID productId, PGvector embedding, String metadata) {
		this.productId = productId;
		this.embedding = embedding;
		this.metadata = metadata;
	}

	/**
	 * 다른 ProductVector와 cosine distance 계산
	 * recommend()에서 1.0 - cosineDistance(...) 형태로 사용됨
	 *
	 * @param other 비교 대상 ProductVector
	 * @return cosine similarity (0~1)
	 */
	public double cosineDistance(ProductVector other) {
		float[] vec1 = this.embedding.toArray();
		float[] vec2 = other.embedding.toArray();

		if (vec1.length != vec2.length) {
			throw new IllegalStateException("Vector lengths do not match.");
		}

		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vec1.length; i++) {
			dotProduct += vec1[i] * vec2[i];
			normA += vec1[i] * vec1[i];
			normB += vec2[i] * vec2[i];
		}

		if (normA == 0.0 || normB == 0.0) {
			return 0.0;
		}

		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));

	}

}
