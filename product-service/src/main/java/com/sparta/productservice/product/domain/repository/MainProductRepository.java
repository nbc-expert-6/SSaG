package com.sparta.productservice.product.domain.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.sparta.productservice.product.domain.entity.MainProduct;

public interface MainProductRepository {
	Optional<MainProduct> getById(UUID mainProductId);

	MainProduct save(MainProduct mainProduct);

	void increaseReviewStat(UUID id, BigDecimal rating);

	void increaseClick(UUID id);
}
