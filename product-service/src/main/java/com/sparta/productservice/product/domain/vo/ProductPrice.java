package com.sparta.productservice.product.domain.vo;

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
public class ProductPrice {

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@Column(name = "shipping_fee")
	private BigDecimal shippingFee;

	@Builder
	public ProductPrice(BigDecimal price, BigDecimal shippingFee) {
		validatePrice(price);
		this.price = price;
		this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
	}

	public BigDecimal getTotalPrice() {
		return price.add(shippingFee);
	}

	public boolean isFreeShipping() {
		if (shippingFee == null) {
			return true;
		}
		return shippingFee.compareTo(BigDecimal.ZERO) == 0;
	}

	private void validatePrice(BigDecimal price) {
		if (price.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("가격은 0보다 작을 수 없습니다.");
		}
	}
}
