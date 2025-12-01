package com.sparta.recommendservice.domain.product_vector.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.query.Param;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pgvector.PGvector;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;
import com.sparta.recommendservice.domain.product_vector.infrastructure.dto.ProductVectorDto;

public interface ProductVectorRepository {

	List<ProductVectorDto> findTopKByEmbedding(@Param("targetEmbedding") PGvector targetEmbedding,
		@Param("candidateSize") int candidateSize) throws JsonProcessingException;

	Optional<ProductVector> findByProductId(UUID productId);

	List<UUID> findAllProductId();

	List<UUID> findUpdatedProductIdsSince(@Param("since") LocalDateTime since);

}
