package com.sparta.recommendservice.domain.product_vector.presentation.common.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationViewResponseDto {

	private UUID productId;
	private String name;
	private BigDecimal price;
	private String imageUrl;
	private String platformType;

}
