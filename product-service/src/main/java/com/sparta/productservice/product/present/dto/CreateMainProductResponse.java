package com.sparta.productservice.product.present.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.productservice.product.app.dto.CreateMainProductResult;

public record CreateMainProductResponse(
	UUID id,
	UUID categoryMediumId,
	String name,
	BigDecimal lowestPrice,
	String imageUrl,
	String brand
) {
	public static CreateMainProductResponse from(CreateMainProductResult result) {
		return new CreateMainProductResponse(
			result.id(),
			result.categoryMediumId(),
			result.name(),
			result.lowestPrice(),
			result.imageUrl(),
			result.brand()
		);
	}
}

