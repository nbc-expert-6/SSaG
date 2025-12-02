package com.sparta.productservice.review.present.dto;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.sparta.productservice.common.dto.PageResponse;
import com.sparta.productservice.review.app.dto.ListReviewResult;
import com.sparta.productservice.review.app.dto.ReviewResult;

public record ListReviewResponse(
	ReviewSummaryDto summary,
	Integer imageTotalCount,
	PageResponse<ReviewItemDto> reviews
) {
	public static ListReviewResponse from(ListReviewResult result) {
		Page<ReviewResult> page = result.reviews();

		Page<ReviewItemDto> reviewItemPage = page.map(ReviewItemDto::from);

		return new ListReviewResponse(
			ReviewSummaryDto.from(result.summary()),
			result.imageTotalCount(),
			PageResponse.of(reviewItemPage)
		);
	}

	public record ReviewSummaryDto(
		String averageRating,
		Long totalCount,
		List<RatingCountDto> ratingDistribution
	) {
		public static ReviewSummaryDto from(ListReviewResult.ReviewSummaryResult result) {
			String avgRating = result.averageRating().toString();

			List<RatingCountDto> distribution = result.ratingDistribution()
				.entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Integer>comparingByKey().reversed())
				.map(e -> new RatingCountDto(e.getKey(), e.getValue()))
				.toList();

			return new ReviewSummaryDto(
				avgRating,
				result.totalCount(),
				distribution
			);
		}
	}

	public record RatingCountDto(
		Integer rating,
		Integer count
	) {}

	public record ReviewItemDto(
		String id,
		String rating,
		String platformType,
		String authorName,
		String title,
		String content,
		List<String> images,
		String createdAt
	) {
		public static ReviewItemDto from(ReviewResult result) {
			return new ReviewItemDto(
				result.id().toString(),
				result.rating().toString(),
				result.platformType().name(),
				result.authorName(),
				result.title(),
				result.content(),
				result.images(),
				result.createdAt().toString()
			);
		}
	}
}
