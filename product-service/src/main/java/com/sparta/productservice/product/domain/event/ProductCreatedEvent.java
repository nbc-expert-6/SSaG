package com.sparta.productservice.product.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.sparta.productservice.common.event.DomainEvent;
import com.sparta.productservice.product.domain.entity.MainProduct;

import lombok.Getter;

@Getter
public class ProductCreatedEvent extends DomainEvent {
	private final UUID mainProductId;
	private final String name;
	private final String brand;
	private final String imageUrl;
	private final BigDecimal lowestPrice;
	private final Integer productCount;
	private final BigDecimal rating;
	private final Long reviewCount;
	private final Long clickCount;
	private final UUID categoryId;
	private final LocalDateTime createdAt;

	public ProductCreatedEvent(
		UUID mainProductId,
		String name,
		String brand,
		String imageUrl,
		BigDecimal lowestPrice,
		Integer productCount,
		BigDecimal rating,
		Long reviewCount,
		Long clickCount,
		UUID categoryId,
		LocalDateTime createdAt
	) {
		super();
		this.mainProductId = mainProductId;
		this.name = name;
		this.brand = brand;
		this.imageUrl = imageUrl;
		this.lowestPrice = lowestPrice;
		this.productCount = productCount;
		this.rating = rating;
		this.reviewCount = reviewCount;
		this.clickCount = clickCount;
		this.categoryId = categoryId;
		this.createdAt = createdAt;
	}

	public static ProductCreatedEvent from(MainProduct mainProduct) {
		return new ProductCreatedEvent(
			mainProduct.getId(),
			mainProduct.getName(),
			mainProduct.getBrand(),
			mainProduct.getImageUrl(),
			mainProduct.getLowestPrice(),
			mainProduct.getProductCount(),
			mainProduct.getReviewRatingAvg(),
			mainProduct.getReviewCount(),
			mainProduct.getClickCount(),
			mainProduct.getCategoryMediumId(),
			mainProduct.getCreatedAt()
		);
	}
}