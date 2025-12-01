package com.sparta.recommendservice.domain.product_vector.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgvector.PGvector;
import com.sparta.recommendservice.domain.product_vector.domain.convert.PGVectorConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
	@Convert(converter = PGVectorConverter.class)
	private PGvector embedding;

	@Column(name = "metadata", columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private JsonNode metadata;

	@Builder
	public ProductVector(UUID productId, PGvector embedding, JsonNode metadata) {
		this.productId = productId;
		this.embedding = embedding;
		this.metadata = metadata;
	}

}

