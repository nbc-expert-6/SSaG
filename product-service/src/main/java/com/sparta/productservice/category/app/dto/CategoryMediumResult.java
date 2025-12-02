package com.sparta.productservice.category.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sparta.productservice.category.domain.entity.CategoryMedium;

public record CategoryMediumResult(
	UUID id,
	UUID categoryLargeId,
	String categoryLargeName,
	String name,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static CategoryMediumResult from(CategoryMedium categoryMedium) {
		return new CategoryMediumResult(
			categoryMedium.getId(),
			categoryMedium.getLargeId(),
			categoryMedium.getLargeName(),
			categoryMedium.getName(),
			categoryMedium.getCreatedAt(),
			categoryMedium.getUpdatedAt()
		);
	}
}
