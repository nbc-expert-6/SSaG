package com.sparta.productservice.review.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.productservice.review.domain.entity.Review;
import com.sparta.productservice.review.domain.repository.dto.RatingCountQuery;

public interface ReviewRepository {
	Page<Review> getReviewsByMainProductId(UUID productId, Pageable pageable);

	Integer countImagesByMainProductId(UUID mainProductId);

	List<RatingCountQuery> getReviewRatingCountByMainProductId(UUID mainProductId);
}
