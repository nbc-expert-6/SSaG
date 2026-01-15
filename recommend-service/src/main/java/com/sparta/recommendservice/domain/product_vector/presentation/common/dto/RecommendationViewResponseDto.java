package com.sparta.recommendservice.domain.product_vector.presentation.common.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.recommendservice.domain.product_vector.application.service.dto.ProductInfoDto;

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
	private Long reviewCount;
	private BigDecimal reviewRatingAvg;

	public static RecommendationViewResponseDto from(UUID productId, ProductInfoDto dto) {
		return new RecommendationViewResponseDto(
			productId,
			dto.name(),
			dto.price(),
			dto.imageUrl(),
			dto.reviewCount(),
			dto.reviewRatingAvg()
		);
	}
}

