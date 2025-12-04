package com.sparta.productservice.product.domain.repository.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record MainProductSearchResult(
	String id,
	String name,
	String brand,
	String imageUrl,
	BigDecimal lowestPrice,
	Integer productCount,
	BigDecimal rating,
	Long reviewCount,
	Long clickCount,
	String categoryId,
	LocalDateTime createdAt
) {
}
