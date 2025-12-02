package com.sparta.productservice.category.present.dto;

import java.util.List;
import java.util.UUID;

import com.sparta.productservice.category.app.dto.CategoryLargeResult;

public record ListCategoryResponse(
	List<CategoryLargeDto> categories
) {

	public record CategoryLargeDto(
		UUID id,
		String name,
		List<CategoryMediumDto> mediumCategories
	) {
	}

	public record CategoryMediumDto(
		UUID id,
		String name
	) {
	}

	public static ListCategoryResponse from(List<CategoryLargeResult> categoryLarges) {
		List<CategoryLargeDto> largeDtos = categoryLarges.stream()
			.map(large -> new CategoryLargeDto(
				large.id(),
				large.name(),
				large.mediumCategories().stream()
					.map(medium -> new CategoryMediumDto(
						medium.id(),
						medium.name()
					))
					.toList()
			))
			.toList();

		return new ListCategoryResponse(largeDtos);
	}
}
