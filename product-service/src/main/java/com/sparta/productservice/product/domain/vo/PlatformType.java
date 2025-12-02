package com.sparta.productservice.product.domain.vo;

import lombok.Getter;

@Getter
public enum PlatformType {
	COUPANG("쿠팡"),
	AUCTION("옥션"),
	G_MARKET("G마켓");

	private final String description;

	PlatformType(String description) {
		this.description = description;
	}
}
