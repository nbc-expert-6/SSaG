package com.sparta.recommendservice.domain.product_vector.infrastructure.external.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductInfoFeignClientResponse(
	UUID productId,
	String brand,
	UUID categoryMediumId,
	BigDecimal price,
	String name,
	String imageUrl,
	Long reviewCount,
	BigDecimal reviewRatingAvg
) {

}