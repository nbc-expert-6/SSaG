package com.sparta.recommendservice.domain.product_vector.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pgvector.PGvector;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;

public interface JpaProductVectorRepository extends JpaRepository<ProductVector, UUID> {

	@Query(value = """
		SELECT product_id, embedding, metadata FROM p_product_vector 
				ORDER BY embedding <=> :targetEmbedding LIMIT :candidateSize
		""", nativeQuery = true)
	List<ProductVector> findTopKByEmbedding(@Param("targetEmbedding") PGvector targetEmbedding,
		@Param("candidateSize") int candidateSize);

}
