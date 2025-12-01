package com.sparta.productservice.product.infra.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.productservice.product.domain.entity.MainProduct;
import com.sparta.productservice.product.domain.entity.Product;

public interface JpaMainProductRepository extends JpaRepository<MainProduct, UUID> {
}
