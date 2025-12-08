package com.sparta.productservice.review.app.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.sparta.productservice.product.domain.vo.PlatformType;
import com.sparta.productservice.review.domain.entity.Review;
import com.sparta.productservice.review.domain.entity.ReviewImage;
import com.sparta.productservice.review.domain.vo.ReviewRating;

public record CreateReviewsCommand(
	UUID mainProductId,
	PlatformType platformType,
	List<CreateReviewDto> reviews
) {

	public record CreateReviewDto(
		String title,
		String content,
		String authorName,
		BigDecimal rating,
		List<String> imageUrls
	) {
		public Review toReview(UUID mainProductId, PlatformType platformType) {

			Review review = Review.builder()
				.mainProductId(mainProductId)
				.platformType(platformType)
				.title(title)
				.content(content)
				.authorName(authorName)
				.rating(new ReviewRating(rating))
				.build();

			if (imageUrls != null && !imageUrls.isEmpty()) {
				imageUrls.stream()
					.map(url -> ReviewImage.builder()
						.imageUrl(url)
						.build())
					.forEach(review::addReviewImage);
			}
			return review;
		}
	}

	public List<Review> toReviews() {
		return reviews.stream()
			.map(dto -> dto.toReview(mainProductId, platformType)) // ✅ 핵심 수정
			.toList();
	}
}
