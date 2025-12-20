package com.sparta.productservice.review.infra.event.message;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.productservice.product.domain.vo.PlatformType;
import com.sparta.productservice.review.app.command.CreateReviewsCommand;

public record CrawledReviewMessage(
	@JsonProperty("main_product_id") UUID mainProductId,
	@JsonProperty("platform") String platform,
	@JsonProperty("reviews") List<CrawledReview> reviews
) {

	public CreateReviewsCommand toCommand() {

		PlatformType platformType =
			PlatformType.valueOf(platform.toUpperCase());

		List<CreateReviewsCommand.CreateReviewDto> reviewCommands =
			reviews.stream()
				.map(review -> new CreateReviewsCommand.CreateReviewDto(
					review.platformReviewId(),
					review.title(),
					review.content(),
					review.authorName(),
					BigDecimal.valueOf(review.rating()),
					review.imageUrls()
				))
				.toList();

		return new CreateReviewsCommand(
			mainProductId,
			platformType,
			reviewCommands
		);
	}

	public record CrawledReview(
		@JsonProperty("id") String platformReviewId,
		@JsonProperty("title") String title,
		@JsonProperty("content") String content,
		@JsonProperty("rating") double rating,
		@JsonProperty("image_urls") List<String> imageUrls,
		@JsonProperty("created_at") String createdAt,
		@JsonProperty("author_name") String authorName
	) {}
}
