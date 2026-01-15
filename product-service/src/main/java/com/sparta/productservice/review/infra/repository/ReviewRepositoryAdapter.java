package com.sparta.productservice.review.infra.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.sparta.productservice.product.domain.vo.PlatformType;
import com.sparta.productservice.review.domain.entity.Review;
import com.sparta.productservice.review.domain.repository.ReviewRepository;
import com.sparta.productservice.review.domain.repository.dto.RatingCountQuery;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {
	private final JpaReviewRepository jpaReviewRepository;
	private final ReviewQueryRepository reviewQueryRepository;

	@Override
	public Page<Review> getReviewsByMainProductId(UUID mainProductId, Pageable pageable) {
		return jpaReviewRepository.findByMainProductId(mainProductId, pageable);
	}

	@Override
	public Integer countImagesByMainProductId(UUID mainProductId) {
		return jpaReviewRepository.countImagesByMainProductId(mainProductId);
	}

	@Override
	public List<RatingCountQuery> getReviewRatingCountByMainProductId(UUID mainProductId) {
		return reviewQueryRepository.getRatingDistributionByMainProductId(mainProductId);
	}

	@Override
	public List<Review> saveAll(List<Review> reviews) {
		return jpaReviewRepository.saveAll(reviews);
	}

	@Override
	public List<String> existsPlatformReviewIds(List<String> platformReviewIds, PlatformType platformType) {
		return reviewQueryRepository.existsPlatformReviewIds(platformReviewIds, platformType);
	}
}
