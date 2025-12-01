package com.sparta.recommendservice.domain.product_vector.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;

public interface JpaProductVectorRepository extends JpaRepository<ProductVector, UUID> {

	@Query(value = """
		SELECT product_id, embedding FROM p_product_vector
		ORDER BY embedding <=> CAST(?1 AS vector)
		LIMIT ?2
		""", nativeQuery = true)
	List<Object[]> findTopKByEmbedding(String targetEmbedding, int candidateSize);

	Optional<ProductVector> findByProductId(UUID productId);

	@Query(value = "SELECT p.product_id FROM p_product_vector p", nativeQuery = true)
	List<UUID> findAllProductId();

	// 마지막 업데이트 이후 변경된 상품 조회
	@Query("SELECT pv.productId FROM ProductVector pv WHERE pv.updatedAt > :since")
	List<UUID> findUpdatedProductIdsSince(@Param("since") LocalDateTime since);

}

