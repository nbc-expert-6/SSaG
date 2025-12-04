package com.sparta.productservice.product.app.port.out.dto;

import java.util.UUID;

public record CategoryInfo(
	UUID mediumCategoryId,
	String mediumCategoryName,
	UUID largeCategoryId,
	String largeCategoryName
) {
}
