package com.sparta.productservice.category.infra.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sparta.productservice.category.domain.entity.CategoryLarge;
import com.sparta.productservice.category.domain.repository.CategoryLargeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryLargeRepositoryAdapter implements CategoryLargeRepository {
	private final JpaCategoryLargeRepository jpaCategoryLargeRepository;

	@Override
	public List<CategoryLarge> getAll() {
		return jpaCategoryLargeRepository.findAll();
	}
}
