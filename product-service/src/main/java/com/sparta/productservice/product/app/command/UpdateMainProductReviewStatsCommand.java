package com.sparta.productservice.product.app.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateMainProductReviewStatsCommand(
	UUID mainProductId,
	List<BigDecimal> newRatings
) {
	public UpdateMainProductReviewStatsCommand(UUID mainProductId, List<BigDecimal> newRatings) {
		if (mainProductId == null) {
			throw new IllegalArgumentException("mainProductId는 필수입니다.");
		}

		if (newRatings == null || newRatings.isEmpty()) {
			throw new IllegalArgumentException("newRatings는 비어있을 수 없습니다.");
		}

		newRatings.forEach(rating -> {
			if (rating == null ||
				rating.compareTo(BigDecimal.ZERO) < 0 ||
				rating.compareTo(BigDecimal.valueOf(5)) > 0) {
				throw new IllegalArgumentException("평점은 0~5 사이여야 합니다: " + rating);
			}
		});

		this.mainProductId = mainProductId;
		this.newRatings = List.copyOf(newRatings); // 불변 리스트로 복사
	}
}
