package com.sparta.productservice.category.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sparta.productservice.category.domain.entity.CategoryMedium;
import com.sparta.productservice.category.domain.repository.CategoryLargeRepository;
import com.sparta.productservice.category.domain.repository.CategoryMediumRepository;
import com.sparta.productservice.category.infra.repository.CategoryLargeRepositoryAdapter;
import com.sparta.productservice.category.infra.repository.CategoryMediumRepositoryAdapter;
import com.sparta.productservice.category.infra.repository.JpaCategoryLargeRepository;
import com.sparta.productservice.category.infra.repository.JpaCategoryMediumRepository;

@Configuration
public class CategoryRepositoryConfig {

	@Bean
	public CategoryLargeRepository categoryLargeRepository(JpaCategoryLargeRepository jpaCategoryLargeRepository) {
		return new CategoryLargeRepositoryAdapter(jpaCategoryLargeRepository);
	}

	@Bean
	public CategoryMediumRepository categoryMediumRepository(JpaCategoryMediumRepository jpaCategoryMediumRepository) {
		return new CategoryMediumRepositoryAdapter(jpaCategoryMediumRepository);
	}
}
