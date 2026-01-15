package com.sparta.productservice.category.app;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.category.app.dto.CategoryLargeResult;
import com.sparta.productservice.category.app.dto.CategoryMediumResult;
import com.sparta.productservice.category.domain.entity.CategoryLarge;
import com.sparta.productservice.category.domain.entity.CategoryMedium;
import com.sparta.productservice.category.domain.repository.CategoryLargeRepository;
import com.sparta.productservice.category.domain.repository.CategoryMediumRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
	private final CategoryLargeRepository categoryLargeRepository;
	private final CategoryMediumRepository categoryMediumRepository;

	public List<CategoryLargeResult> getAllCategories() {
		List<CategoryLarge> categoryLargeResults = categoryLargeRepository.getAll();

		return categoryLargeResults
			.stream()
			.map(c -> CategoryLargeResult.from(c))
			.toList();
	}

	public CategoryMediumResult getCategoryMediumById(UUID categoryMediumId) {
		CategoryMedium categoryMedium = categoryMediumRepository.getById(categoryMediumId)
			.orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다."));

		return CategoryMediumResult.from(categoryMedium);
	}
}
