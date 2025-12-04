package com.sparta.productservice.product.present.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.productservice.product.app.command.CreateMainProductCommand;

public record CreateMainProductRequest(
	UUID categoryMediumId,
	String name,
	BigDecimal lowestPrice,
	String imageUrl,
	String brand
) {
	public CreateMainProductCommand toCommand() {
		return new CreateMainProductCommand(
			categoryMediumId,
			name,
			lowestPrice,
			imageUrl,
			brand
		);
	}
}

