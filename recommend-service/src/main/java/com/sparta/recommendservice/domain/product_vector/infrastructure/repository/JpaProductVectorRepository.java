package com.sparta.recommendservice.domain.product_vector.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;

public interface JpaProductVectorRepository extends JpaRepository<ProductVector, UUID> {

	@Query(value = """
		SELECT product_id, embedding, metadata FROM p_product_vector
		ORDER BY embedding <=> CAST(?1 AS vector)
		LIMIT ?2
		""", nativeQuery = true)
	List<Object[]> findTopKByEmbedding(String targetEmbedding, int candidateSize);

	Optional<ProductVector> findByProductId(UUID productId);

	@Query(value = "SELECT p.product_id FROM p_product_vector p", nativeQuery = true)
	List<UUID> findAllProductId();

}

