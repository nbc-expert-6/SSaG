package com.sparta.productservice.category.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.category.app.dto.CategoryLargeResult;
import com.sparta.productservice.category.domain.entity.CategoryLarge;
import com.sparta.productservice.category.domain.repository.CategoryLargeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
	private final CategoryLargeRepository categoryLargeRepository;

	public List<CategoryLargeResult> getAllCategories() {
		List<CategoryLarge> categoryLargeResults = categoryLargeRepository.getAll();

		return categoryLargeResults
			.stream()
			.map(c -> CategoryLargeResult.from(c))
			.toList();
	}
}
