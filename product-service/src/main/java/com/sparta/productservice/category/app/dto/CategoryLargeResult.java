package com.sparta.productservice.category.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sparta.productservice.category.domain.entity.CategoryLarge;

public record CategoryLargeResult(
	UUID id,
	String name,
	List<CategoryMediumResult> mediumCategories,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static CategoryLargeResult from(CategoryLarge categoryLarge) {
		return new CategoryLargeResult(
			categoryLarge.getId(),
			categoryLarge.getName(),
			categoryLarge.getCategoryMediums().stream()
				.map(CategoryMediumResult::from)
				.toList(),
			categoryLarge.getCreatedAt(),
			categoryLarge.getUpdatedAt()
		);
	}
}
