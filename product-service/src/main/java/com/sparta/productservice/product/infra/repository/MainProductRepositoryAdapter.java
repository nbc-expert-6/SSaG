package com.sparta.productservice.product.infra.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@Override
	public void increaseReviewStatBatch(UUID id, Long count, BigDecimal totalRating) {
		jpaMainProductRepository.increaseReviewStatBatch(id, count, totalRating);
	}

	@Override
	public void increaseClick(UUID mainProductId) {
		jpaMainProductRepository.increaseClick(mainProductId);
	}

	@Override
	public Page<MainProduct> getAll(Pageable pageable) {
		return jpaMainProductRepository.findAll(pageable);
	}
}
