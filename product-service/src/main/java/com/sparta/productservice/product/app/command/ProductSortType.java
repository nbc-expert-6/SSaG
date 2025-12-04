package com.sparta.productservice.product.app.command;

import lombok.Getter;

@Getter
public enum ProductSortType {
	POPULAR("인기순"),
	PRICE_LOW("낮은 가격순"),
	PRICE_HIGH("높은 가격순"),
	REVIEW("리뷰 많은 순"),
	RECENT("등록일순");

	private final String description;

	ProductSortType(String description) {
		this.description = description;
	}
}