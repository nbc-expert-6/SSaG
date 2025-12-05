package com.sparta.productservice.product.app.command;

import java.util.List;
import java.util.UUID;

import com.sparta.productservice.common.dto.PageSizeType;

import lombok.Builder;

@Builder
public record SearchMainProductCommand(
	List<UUID> productIds,
	String productName,
	List<UUID> categoryIds,
	Integer pageNumber,
	PageSizeType pageSize,
	ProductSortType pageSort
) {
}
