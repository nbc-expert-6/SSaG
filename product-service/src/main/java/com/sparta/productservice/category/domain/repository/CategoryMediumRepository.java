package com.sparta.productservice.category.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.sparta.productservice.category.domain.entity.CategoryMedium;

public interface CategoryMediumRepository {
	Optional<CategoryMedium> getById(UUID id);
}
