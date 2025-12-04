package com.sparta.productservice.product.infra.search.mapper;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.sparta.productservice.product.app.command.ProductSortType;

@Component
public class MainProductSortMapper {
	public Sort toEsSort(ProductSortType sortType) {

		if (sortType == null) {
			return Sort.by(Sort.Order.desc("clickCount"));
		}

		return switch (sortType) {
			case POPULAR ->
				Sort.by(Sort.Order.desc("clickCount"));

			case PRICE_LOW ->
				Sort.by(Sort.Order.asc("lowestPrice"));

			case PRICE_HIGH ->
				Sort.by(Sort.Order.desc("lowestPrice"));

			case REVIEW ->
				Sort.by(Sort.Order.desc("reviewCount"));

			case RECENT ->
				Sort.by(Sort.Order.desc("createdAt"));

			// ✅ 혹시 enum이 나중에 추가돼도 안전하게 RECENT로 fallback
			default ->
				Sort.by(Sort.Order.desc("clickCount"));
		};
	}
}
