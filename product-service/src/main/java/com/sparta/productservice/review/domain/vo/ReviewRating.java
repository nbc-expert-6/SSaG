package com.sparta.productservice.review.domain.vo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewRating {

	@Column(name = "rating", nullable = false, precision = 2, scale = 1)
	private BigDecimal value;

	@Builder
	public ReviewRating(BigDecimal value) {
		validateRating(value);
		this.value = value;
	}

	private void validateRating(BigDecimal rating) {
		if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
			throw new IllegalArgumentException("평점은 0.0에서 5.0 사이여야 합니다.");
		}

		if (rating.multiply(BigDecimal.TEN).remainder(BigDecimal.valueOf(5)).compareTo(BigDecimal.ZERO) != 0) {
			throw new IllegalArgumentException("평점은 0.5 단위로 입력해야 합니다.");
		}
	}
}
