package com.sparta.productservice.product.domain.vo;

import lombok.Getter;

@Getter
public enum PlatformType {
	COUPANG("쿠팡");
	// AUCTION("옥션"),
	// ELEVEN_ST("11번가");

	private final String description;

	PlatformType(String description) {
		this.description = description;
	}
}
