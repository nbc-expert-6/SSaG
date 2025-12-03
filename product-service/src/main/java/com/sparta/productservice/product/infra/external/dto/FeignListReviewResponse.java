package com.sparta.productservice.product.infra.external.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sparta.productservice.common.dto.PageResponse;
import com.sparta.productservice.product.app.service.dto.ReviewInfo;
import com.sparta.productservice.product.domain.vo.PlatformType;

public record FeignListReviewResponse(
	FeignReviewSummary summary,
	Integer imageTotalCount,
	PageResponse<FeignReviewItem> reviews
) {
	public List<ReviewInfo> toReviewInfoList() {
		return reviews.getContent().stream()
			.map(FeignReviewItem::toReviewInfo)
			.toList();
	}

	private record FeignReviewSummary(
		String averageRating,
		Integer totalCount,
		List<FeignRatingCount> feignRatingCount
	) {
	}

	private record FeignRatingCount(
		Integer rating,
		Integer count
	) {
	}

	private record FeignReviewItem(
		String id,
		String rating,
		String platformType,
		String authorName,
		String title,
		String content,
		List<String> images,
		String createdAt
	) {
		public ReviewInfo toReviewInfo() {
			return new ReviewInfo(
				UUID.fromString(this.id),
				new BigDecimal(this.rating),
				PlatformType.valueOf(this.platformType),
				this.authorName,
				this.title,
				this.content,
				this.images,
				LocalDateTime.parse(this.createdAt)
			);
		}
	}
}
