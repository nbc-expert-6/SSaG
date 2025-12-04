package com.sparta.productservice.product.present.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.productservice.product.app.command.CreateMainProductCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMainProductRequest(
	@NotNull
	UUID categoryMediumId,
	@NotBlank
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

