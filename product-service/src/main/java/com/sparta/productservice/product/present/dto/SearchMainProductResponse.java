package com.sparta.productservice.product.present.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import com.sparta.productservice.common.dto.PageResponse;
import com.sparta.productservice.product.domain.repository.dto.MainProductSearchResult;

public record SearchMainProductResponse(
	PageResponse<ProductItemDto> products
) {
	public static SearchMainProductResponse from(Page<MainProductSearchResult> page) {
		Page<ProductItemDto> productPage = page.map(ProductItemDto::from);

		return new SearchMainProductResponse(
			PageResponse.of(productPage)
		);
	}

	public record ProductItemDto(
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
		public static ProductItemDto from(MainProductSearchResult result) {
			return new ProductItemDto(
				result.id(),
				result.name(),
				result.brand(),
				result.imageUrl(),
				result.lowestPrice(),
				result.productCount(),
				result.rating(),
				result.reviewCount(),
				result.clickCount(),
				result.categoryId(),
				result.createdAt()
			);
		}
	}
}