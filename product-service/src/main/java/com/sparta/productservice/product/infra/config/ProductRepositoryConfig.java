package com.sparta.productservice.product.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sparta.productservice.product.domain.repository.MainProductRepository;
import com.sparta.productservice.product.infra.repository.JpaMainProductRepository;
import com.sparta.productservice.product.infra.repository.MainProductRepositoryAdapter;

@Configuration
public class ProductRepositoryConfig {

	@Bean
	public MainProductRepository mainProductRepository(JpaMainProductRepository jpaMainProductRepository) {
		return new MainProductRepositoryAdapter(jpaMainProductRepository);
	}
}
