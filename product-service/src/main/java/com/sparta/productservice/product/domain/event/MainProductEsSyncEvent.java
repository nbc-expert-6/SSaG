package com.sparta.productservice.product.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sparta.productservice.product.domain.entity.MainProduct;

import lombok.Getter;

@Getter
public class MainProductEsSyncEvent {
	private final List<MainProductDto> products;

	public MainProductEsSyncEvent(List<MainProductDto> products) {
		this.products = products;
	}

	@Getter
	public static class MainProductDto {
		private final UUID id;
		private final String name;
		private final String brand;
		private final UUID categoryId;
		private final BigDecimal lowestPrice;
		private final int productCount;
		private final BigDecimal rating;
		private final long reviewCount;
		private final long clickCount;
		private final String imageUrl;
		private final LocalDateTime createdAt;
		private final boolean deleted;

		public MainProductDto(UUID id, String name, String brand, UUID categoryId,
			BigDecimal lowestPrice, int productCount, BigDecimal rating,
			long reviewCount, long clickCount,
			String imageUrl, LocalDateTime createdAt, boolean deleted) {
			this.id = id;
			this.name = name;
			this.brand = brand;
			this.categoryId = categoryId;
			this.lowestPrice = lowestPrice;
			this.productCount = productCount;
			this.rating = rating;
			this.reviewCount = reviewCount;
			this.clickCount = clickCount;
			this.imageUrl = imageUrl;
			this.createdAt = createdAt;
			this.deleted = deleted;
		}

		public static MainProductDto from(MainProduct entity) {
			return new MainProductDto(
				entity.getId(),
				entity.getName(),
				entity.getBrand(),
				entity.getCategoryMediumId(), // categoryId
				entity.getLowestPrice(),
				entity.getProductCount(),
				entity.getReviewRatingAvg(), // rating
				entity.getReviewCount(),
				entity.getClickCount(),
				entity.getImageUrl(),
				entity.getCreatedAt(),
				entity.isDeleted()
			);
		}
	}
}
