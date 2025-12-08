package com.sparta.productservice.common.message;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.sparta.productservice.product.domain.vo.PlatformType;
import com.sparta.productservice.review.domain.entity.Review;

public record ReviewCreatedMessage(
	UUID mainProductId,
	PlatformType platformType,
	List<ReviewCreated> reviews
) {
	public ReviewCreatedMessage {
		if (mainProductId == null) {
			throw new IllegalArgumentException("mainProductId는 필수입니다.");
		}
		if (platformType == null) {
			throw new IllegalArgumentException("platformType은 필수입니다.");
		}
		if (reviews == null || reviews.isEmpty()) {
			throw new IllegalArgumentException("reviews는 최소 1개 이상이어야 합니다.");
		}
	}

	public static ReviewCreatedMessage from(
		UUID mainProductId,
		PlatformType platformType,
		List<Review> reviews
	) {
		return new ReviewCreatedMessage(
			mainProductId,
			platformType,
			reviews.stream()
				.map(ReviewCreated::from)
				.toList()
		);
	}

	public record ReviewCreated(
		UUID reviewId,
		BigDecimal rating,
		String authorName
	) {
		public ReviewCreated {
			if (reviewId == null) {
				throw new IllegalArgumentException("reviewId는 필수입니다.");
			}
			if (rating == null) {
				throw new IllegalArgumentException("rating은 필수입니다.");
			}
		}

		public static ReviewCreated from(Review review) {
			return new ReviewCreated(
				review.getId(),
				review.getRating(),
				review.getAuthorName()
			);
		}
	}
}