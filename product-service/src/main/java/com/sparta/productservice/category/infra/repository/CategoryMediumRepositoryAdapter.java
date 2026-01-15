package com.sparta.productservice.category.infra.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sparta.productservice.category.domain.entity.CategoryMedium;
import com.sparta.productservice.category.domain.repository.CategoryMediumRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryMediumRepositoryAdapter implements CategoryMediumRepository {
	private final JpaCategoryMediumRepository jpaCategoryMediumRepository;

	@Override
	public Optional<CategoryMedium> getById(UUID id) {
		return jpaCategoryMediumRepository.findById(id);
	}
}
