package com.sparta.recommendservice.domain.product_vector.application.service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductInfoDto(
	UUID productId,
	String brand,
	UUID categoryMediumId,
	BigDecimal price
) {
}
