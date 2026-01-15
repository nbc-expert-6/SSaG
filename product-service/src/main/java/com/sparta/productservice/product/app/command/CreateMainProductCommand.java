package com.sparta.productservice.product.app.command;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.productservice.product.domain.entity.MainProduct;

public record CreateMainProductCommand(
	UUID categoryMediumId,
	String name,
	BigDecimal lowestPrice,
	String imageUrl,
	String brand
) {
	public MainProduct toMainProduct() {
		return MainProduct.builder()
			.categoryMediumId(categoryMediumId)
			.name(name)
			.lowestPrice(lowestPrice)
			.imageUrl(imageUrl)
			.brand(brand)
			.build();
	}
}
