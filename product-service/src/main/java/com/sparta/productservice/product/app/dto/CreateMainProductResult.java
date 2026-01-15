package com.sparta.productservice.product.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.productservice.product.domain.entity.MainProduct;

public record CreateMainProductResult(
	UUID id,
	UUID categoryMediumId,
	String name,
	BigDecimal lowestPrice,
	String imageUrl,
	String brand
) {
	public static CreateMainProductResult from(MainProduct mainProduct) {
		return new CreateMainProductResult(
			mainProduct.getId(),
			mainProduct.getCategoryMediumId(),
			mainProduct.getName(),
			mainProduct.getLowestPrice(),
			mainProduct.getImageUrl(),
			mainProduct.getBrand()
		);
	}
}

