package com.sparta.productservice.category.present.dto;

import java.util.UUID;

import com.sparta.productservice.category.app.dto.CategoryMediumResult;
import com.sparta.productservice.category.domain.entity.CategoryMedium;

public record GetCategoryResponse(
	UUID mediumId,
	String mediumName,
	UUID largeId,
	String largeName
) {
	public static GetCategoryResponse from(CategoryMediumResult categoryMediumResult) {
		return new GetCategoryResponse(
			categoryMediumResult.id(),
			categoryMediumResult.name(),
			categoryMediumResult.categoryLargeId(),
			categoryMediumResult.categoryLargeName()
		);
	}
}
