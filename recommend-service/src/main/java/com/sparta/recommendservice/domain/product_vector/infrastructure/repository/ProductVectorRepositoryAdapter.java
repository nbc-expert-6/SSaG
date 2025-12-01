package com.sparta.recommendservice.domain.product_vector.infrastructure.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;
import com.sparta.recommendservice.domain.product_vector.domain.repository.ProductVectorRepository;
import com.sparta.recommendservice.domain.product_vector.infrastructure.dto.ProductVectorDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductVectorRepositoryAdapter implements ProductVectorRepository {

	private final JpaProductVectorRepository jpaProductVectorRepository;

	@Override
	public List<ProductVectorDto> findTopKByEmbedding(PGvector targetEmbedding, int candidateSize) throws
		JsonProcessingException {
		List<Object[]> rows = jpaProductVectorRepository.findTopKByEmbedding(targetEmbedding.toString(), candidateSize);
		List<ProductVectorDto> result = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();

		for (Object[] row : rows) {
			UUID pid = (UUID)row[0];
			String embeddingStr = row[1].toString();
			JsonNode metadata = null;

			if (row[2] != null) {
				metadata = objectMapper.readTree(row[2].toString());
			}

			float[] vec = parsePGVectorString(embeddingStr);
			result.add(new ProductVectorDto(pid, vec, metadata));
		}

		return result;
	}

	private float[] parsePGVectorString(String vectorStr) {
		vectorStr = vectorStr.replaceAll("[\\[\\]]", "");
		String[] parts = vectorStr.split(",");

		float[] vec = new float[parts.length];
		for (int i = 0; i < parts.length; i++) {
			vec[i] = Float.parseFloat(parts[i]);
		}

		return vec;
	}

	@Override
	public Optional<ProductVector> findByProductId(UUID productId) {
		return jpaProductVectorRepository.findByProductId(productId);
	}

	@Override
	public List<UUID> findAllProductId() {
		return jpaProductVectorRepository.findAllProductId();
	}

}

