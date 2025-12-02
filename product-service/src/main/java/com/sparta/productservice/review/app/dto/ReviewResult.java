package com.sparta.productservice.review.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sparta.productservice.product.domain.vo.PlatformType;
import com.sparta.productservice.review.domain.entity.Review;

public record ReviewResult(
	UUID id,
	BigDecimal rating,
	PlatformType platformType,
	String authorName,
	String title,
	String content,
	List<String> images,
	LocalDateTime createdAt
) {
	public static ReviewResult from(Review review) {
		return new ReviewResult(
			review.getId(),
			review.getRating(),
			review.getPlatformType(),
			review.getAuthorName(),
			review.getTitle(),
			review.getContent(),
			review.getImageUrls(),
			review.getCreatedAt()
		);
	}
}
