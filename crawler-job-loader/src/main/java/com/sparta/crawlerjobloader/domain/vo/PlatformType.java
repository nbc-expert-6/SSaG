package com.sparta.crawlerjobloader.domain.vo;

import lombok.Getter;

@Getter
public enum PlatformType {
	COUPANG("쿠팡"),
	AUCTION("옥션"),
	GMARKET("G마켓"),
	ELEVENST("11번가");

	private final String description;

	PlatformType(String description) {
		this.description = description;
	}
}
