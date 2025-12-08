package com.sparta.recommendservice.domain.product_vector.application.service.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.recommendservice.domain.product_vector.infrastructure.external.dto.ProductInfoFeignClientResponse;

public record ProductInfoDto(
	UUID productId,
	String brand,
	UUID categoryMediumId,
	BigDecimal price,
	String name,
	String imageUrl,
	Long reviewCount,
	BigDecimal reviewRatingAvg
) {
	public static ProductInfoDto from(ProductInfoFeignClientResponse dto) {
		return new ProductInfoDto(
			dto.productId(),
			dto.brand(),
			dto.categoryMediumId(),
			dto.price(),
			dto.name(),
			dto.imageUrl(),
			dto.reviewCount(),
			dto.reviewRatingAvg()
		);
	}
}


