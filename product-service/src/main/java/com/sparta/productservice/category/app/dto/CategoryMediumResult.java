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

	// Entity -> Result 변환
	public static CategoryMediumResult from(CategoryMedium categoryMedium) {
		return new CategoryMediumResult(
			categoryMedium.getId(),
			categoryMedium.getCategoryLarge().getId(),
			categoryMedium.getCategoryLarge().getName(),
			categoryMedium.getName(),
			categoryMedium.getCreatedAt(),
			categoryMedium.getUpdatedAt()
		);
	}
}
