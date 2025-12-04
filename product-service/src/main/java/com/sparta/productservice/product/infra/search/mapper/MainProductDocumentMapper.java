package com.sparta.productservice.product.infra.search.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.sparta.productservice.product.domain.event.ProductCreatedEvent;
import com.sparta.productservice.product.domain.repository.dto.MainProductSearchResult;
import com.sparta.productservice.product.infra.search.document.MainProductDocument;

@Component
public class MainProductDocumentMapper {
	public MainProductSearchResult toSearchResult(MainProductDocument document) {
		return MainProductSearchResult.builder()
			.id(document.getId())
			.name(document.getName())
			.brand(document.getBrand())
			.imageUrl(document.getImageUrl())
			.lowestPrice(document.getLowestPrice())
			.productCount(document.getProductCount())
			.rating(document.getRating())
			.reviewCount(document.getReviewCount())
			.clickCount(document.getClickCount())
			.categoryId(document.getCategoryId())
			.createdAt(LocalDateTime.ofInstant(document.getCreatedAt(), ZoneId.systemDefault()))
			.build();
	}

	public MainProductDocument toDocument(ProductCreatedEvent event) {
		return MainProductDocument.builder()
			.id(event.getMainProductId().toString())
			.name(event.getName())
			.brand(event.getBrand())
			.imageUrl(event.getImageUrl())
			.lowestPrice(event.getLowestPrice())
			.productCount(event.getProductCount())
			.rating(event.getRating())
			.reviewCount(event.getReviewCount())
			.clickCount(event.getClickCount())
			.categoryId(event.getCategoryId().toString())
			.deleted(false)
			.createdAt(event.getCreatedAt().toInstant(ZoneOffset.UTC))
			.build();
	}
}