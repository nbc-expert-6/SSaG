package com.sparta.productservice.product.infra.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sparta.productservice.product.domain.entity.MainProduct;
import com.sparta.productservice.product.domain.repository.MainProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MainProductRepositoryAdapter implements MainProductRepository {
	private final JpaMainProductRepository jpaMainProductRepository;

	@Override
	public Optional<MainProduct> getById(UUID mainProductId) {
		return jpaMainProductRepository.findById(mainProductId);
	}

	@Override
	public MainProduct save(MainProduct mainProduct) {
		return jpaMainProductRepository.save(mainProduct);
	}
}
