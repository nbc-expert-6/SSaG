package com.sparta.productservice.product.domain.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.productservice.product.domain.entity.MainProduct;

public interface MainProductRepository {
	Optional<MainProduct> getById(UUID mainProductId);

	MainProduct save(MainProduct mainProduct);

	void increaseReviewStatBatch(UUID id, Long count, BigDecimal totalRating);

	void increaseClick(UUID id);

	Page<MainProduct> getAll(Pageable pageable);
}
