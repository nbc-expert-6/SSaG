package com.sparta.productservice.product.infra.external.dto;

import java.util.UUID;

import com.sparta.productservice.product.app.service.dto.CategoryInfo;

public record FeignGetCategoryResponse(
	UUID mediumId,
	String mediumName,
	UUID largeId,
	String largeName
) {
	public CategoryInfo toCategoryInfo() {
		return new CategoryInfo(
			this.mediumId,
			this.mediumName,
			this.largeId,
			this.largeName
		);
	}
}
