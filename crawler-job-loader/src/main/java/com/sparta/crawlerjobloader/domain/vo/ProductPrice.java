package com.sparta.crawlerjobloader.domain.vo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
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

}
