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

		var m = dto.mainProduct();
		var categoryMediumId = m.category() != null && m.category().medium() != null
			? m.category().medium().id()
			: null;

		BigDecimal price = null;
		try {
			if (m.lowestPrice() != null) {
				String raw = m.lowestPrice().replaceAll("[^0-9]", "");
				if (!raw.isEmpty())
					price = new BigDecimal(raw);
			}
		} catch (Exception ignored) {
			price = BigDecimal.ZERO;
		}

		Long reviewCount = dto.reviews() != null ? (long)dto.reviews().size() : 0;

		BigDecimal reviewRatingAvg = BigDecimal.ZERO;
		if (dto.reviews() != null && !dto.reviews().isEmpty()) {
			double avg = dto.reviews().stream()
				.filter(r -> r.rating() != null)
				.mapToDouble(r -> r.rating().doubleValue())
				.average().orElse(0.0);
			reviewRatingAvg = BigDecimal.valueOf(avg);
		}

		return new ProductInfoDto(
			m.id(),
			m.brand(),
			categoryMediumId,
			price,
			m.name(),
			m.imageUrl(),
			reviewCount,
			reviewRatingAvg
		);
	}
}



