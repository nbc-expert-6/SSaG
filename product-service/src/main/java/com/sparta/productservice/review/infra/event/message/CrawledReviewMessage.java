package com.sparta.productservice.review.infra.event.message;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.productservice.product.domain.vo.PlatformType;
import com.sparta.productservice.review.app.command.CreateReviewsCommand;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CrawledReviewMessage {

	@JsonProperty("main_product_id")
	private UUID mainProductId;

	@JsonProperty("platform")
	private String platform;

	@JsonProperty("reviews")
	private List<CrawledReview> reviews;

	public CreateReviewsCommand toCommand() {

		PlatformType platformType =
			PlatformType.valueOf(platform.toUpperCase());

		List<CreateReviewsCommand.CreateReviewDto> reviewCommands =
			reviews.stream()
				.map(review -> new CreateReviewsCommand.CreateReviewDto(
					review.getPlatformReviewId(),
					review.getTitle(),
					review.getContent(),
					review.getAuthorName(),
					BigDecimal.valueOf(review.getRating()),
					review.getImageUrls()
				))
				.toList();

		return new CreateReviewsCommand(
			mainProductId,
			platformType,
			reviewCommands
		);
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	public static class CrawledReview {

		@JsonProperty("id")
		private String platformReviewId;

		@JsonProperty("title")
		private String title;

		@JsonProperty("content")
		private String content;

		@JsonProperty("rating")
		private double rating;

		@JsonProperty("image_urls")
		private List<String> imageUrls;

		@JsonProperty("created_at")
		private String createdAt;

		@JsonProperty("author_name")
		private String authorName;
	}
}
