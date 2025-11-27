package com.sparta.productservice.common.dto;

import lombok.Getter;

@Getter
public enum PageSizeType {
	SIZE_10(10),
	SIZE_30(30),
	SIZE_60(60);

	private final int value;

	PageSizeType(int value) {
		this.value = value;
	}
}

