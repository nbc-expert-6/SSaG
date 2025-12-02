package com.sparta.productservice.review.app.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.sparta.productservice.review.domain.entity.Review;
import com.sparta.productservice.review.domain.repository.dto.RatingCountQuery;

public record ListReviewResult(
	Page<ReviewResult> reviews,
	ReviewSummaryResult summary,
	Integer imageTotalCount
) {
	public static ListReviewResult from(
		Page<Review> reviewPage,
		Integer imageTotalCount,
		List<RatingCountQuery> ratingCountQueries
	) {
		Page<ReviewResult> reviewResults = reviewPage.map(ReviewResult::from);

		ReviewSummaryResult summary = calculateSummary(ratingCountQueries, reviewPage.getTotalElements());

		return new ListReviewResult(reviewResults, summary, imageTotalCount);
	}

	private static ReviewSummaryResult calculateSummary(
		List<RatingCountQuery> ratingCountQueries,
		Long totalCount
	) {
		if (ratingCountQueries.isEmpty() || totalCount == 0) {
			return ReviewSummaryResult.of(BigDecimal.ZERO, totalCount, initializeRatingDistribution());
		}

		Map<Integer, Integer> ratingDistribution = ratingCountQueries.stream()
			.collect(Collectors.toMap(
				RatingCountQuery::rating,
				query -> query.count().intValue(),
				(a, b) -> a,
				LinkedHashMap::new
			));

		for (int i = 1; i <= 5; i++) {
			ratingDistribution.putIfAbsent(i, 0);
		}

		BigDecimal totalRatingSum = ratingCountQueries.stream()
			.map(query -> BigDecimal.valueOf(query.rating())
				.multiply(BigDecimal.valueOf(query.count())))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal averageRating = totalRatingSum.divide(
			BigDecimal.valueOf(totalCount),
			1,
			RoundingMode.HALF_UP
		);

		return ReviewSummaryResult.of(
			averageRating,
			totalCount,
			ratingDistribution
		);
	}

	private static Map<Integer, Integer> initializeRatingDistribution() {
		Map<Integer, Integer> distribution = new LinkedHashMap<>();
		for (int i = 1; i <= 5; i++) {
			distribution.put(i, 0);
		}
		return distribution;
	}

	public record ReviewSummaryResult(
		BigDecimal averageRating,
		Long totalCount,
		Map<Integer, Integer> ratingDistribution
	) {
		private static ReviewSummaryResult of(
			BigDecimal averageRating,
			Long totalCount,
			Map<Integer, Integer> ratingDistribution
		) {
			return new ReviewSummaryResult(averageRating, totalCount, ratingDistribution);
		}
	}
}