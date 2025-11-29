package com.sparta.recommendservice.domain.product_vector.domain.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.pgvector.PGvector;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;

public interface ProductVectorRepository {

	List<ProductVector> findTopKByEmbedding(@Param("targetEmbedding") PGvector targetEmbedding,
		@Param("candidateSize") int candidateSize);

}
