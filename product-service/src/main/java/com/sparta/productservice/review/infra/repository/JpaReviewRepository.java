package com.sparta.productservice.review.infra.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.productservice.review.domain.entity.Review;

public interface JpaReviewRepository extends JpaRepository<Review, UUID> {
	@EntityGraph(attributePaths = {"images"})
	Page<Review> findByMainProductId(UUID mainProductId, Pageable pageable);

	@Query("SELECT COUNT(ri) FROM ReviewImage ri "
		+ "WHERE ri.review.mainProductId = :mainProductId")
	Integer countImagesByMainProductId(@Param("mainProductId") UUID mainProductId);
}
