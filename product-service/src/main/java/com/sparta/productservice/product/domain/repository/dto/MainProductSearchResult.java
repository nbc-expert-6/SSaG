package com.sparta.productservice.product.domain.repository.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.productservice.product.domain.entity.MainProduct;

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
	public static MainProductSearchResult from(MainProduct mainProduct) {
		return MainProductSearchResult.builder()
			.id(mainProduct.getId().toString())
			.name(mainProduct.getName())
			.brand(mainProduct.getBrand())
			.imageUrl(mainProduct.getImageUrl())
			.lowestPrice(mainProduct.getLowestPrice())
			.productCount(mainProduct.getProductCount())
			.rating(mainProduct.getReviewRatingAvg())
			.reviewCount(mainProduct.getReviewCount())
			.clickCount(mainProduct.getClickCount())
			.categoryId(mainProduct.getCategoryMediumId().toString())
			.createdAt(mainProduct.getCreatedAt())
			.build();
	}
}
