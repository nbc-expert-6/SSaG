package com.sparta.recommendservice.domain.product_vector.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pgvector.PGvector;
import com.sparta.recommendservice.domain.product_vector.domain.convert.PGVectorConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

}

