package com.sparta.recommendservice.domain.product_vector.infrastructure.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.pgvector.PGvector;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;
import com.sparta.recommendservice.domain.product_vector.domain.repository.ProductVectorRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductVectorRepositoryAdapter implements ProductVectorRepository {

	private final JpaProductVectorRepository jpaProductVectorRepository;

	@Override
	public List<ProductVector> findTopKByEmbedding(PGvector targetEmbedding, int candidateSize) {
		return jpaProductVectorRepository.findTopKByEmbedding(targetEmbedding, candidateSize);
	}
}
