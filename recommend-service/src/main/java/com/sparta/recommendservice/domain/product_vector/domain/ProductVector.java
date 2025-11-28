package com.sparta.recommendservice.domain.product_vector.domain;

import java.util.UUID;

import com.pgvector.PGvector;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_product_vector", schema = "recommend_service_db")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVector {

	@Id
	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "embedding", columnDefinition = "public.vector(128)", nullable = false)
	private PGvector embedding;

	@Column(name = "metadta", columnDefinition = "jsonb", nullable = false)
	private String metadata;

}
