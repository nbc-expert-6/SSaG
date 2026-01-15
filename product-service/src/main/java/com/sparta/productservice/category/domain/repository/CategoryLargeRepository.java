package com.sparta.productservice.category.domain.repository;

import java.util.List;

import com.sparta.productservice.category.domain.entity.CategoryLarge;

public interface CategoryLargeRepository {
	List<CategoryLarge> getAll();
}
