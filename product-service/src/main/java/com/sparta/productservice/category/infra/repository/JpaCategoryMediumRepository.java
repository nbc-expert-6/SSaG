package com.sparta.productservice.category.infra.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.productservice.category.domain.entity.CategoryMedium;

public interface JpaCategoryMediumRepository extends JpaRepository<CategoryMedium, UUID> {
}
