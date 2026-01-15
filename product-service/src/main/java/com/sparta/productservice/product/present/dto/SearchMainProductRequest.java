package com.sparta.productservice.product.present.dto;

import java.util.List;
import java.util.UUID;

import com.sparta.productservice.common.dto.PageSizeType;
import com.sparta.productservice.product.app.command.ProductSortType;
import com.sparta.productservice.product.app.command.SearchMainProductCommand;

import jakarta.validation.constraints.Min;

public record SearchMainProductRequest(
	List<UUID> productIds,

	String productName,

	List<UUID> categoryIds,

	@Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
	Integer page,

	PageSizeType size,

	ProductSortType sort
) {
	public SearchMainProductCommand toCommand() {
		return SearchMainProductCommand.builder()
			.productIds(productIds)
			.productName(productName)
			.categoryIds(categoryIds)
			.pageNumber(page != null ? page : 0)
			.pageSize(size != null ? size : PageSizeType.SIZE_30)
			.pageSort(sort != null ? sort : ProductSortType.POPULAR)
			.build();
	}
}
