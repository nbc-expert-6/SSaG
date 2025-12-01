package com.sparta.productservice.product.app.service;

import java.util.UUID;

import com.sparta.productservice.product.app.service.dto.CategoryInfo;

public interface CategoryClient {
	CategoryInfo getCategoryByMediumId(UUID id);
}
