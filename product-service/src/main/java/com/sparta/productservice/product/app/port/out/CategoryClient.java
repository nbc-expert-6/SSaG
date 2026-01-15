package com.sparta.productservice.product.app.port.out;

import java.util.UUID;

import com.sparta.productservice.product.app.port.out.dto.CategoryInfo;

public interface CategoryClient {
	CategoryInfo getCategoryByMediumId(UUID id);
}
