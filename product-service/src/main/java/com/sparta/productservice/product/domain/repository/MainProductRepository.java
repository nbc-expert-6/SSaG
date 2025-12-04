package com.sparta.productservice.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.sparta.productservice.product.domain.entity.MainProduct;

public interface MainProductRepository {
	Optional<MainProduct> getById(UUID mainProductId);

	MainProduct save(MainProduct mainProduct);
}
