package com.sparta.recommendservice.domain.product_vector.infrastructure.external.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductInfoFeignClientResponse(
	@JsonProperty("mainProduct") MainProduct mainProduct,
	@JsonProperty("priceComparison") PriceComparison priceComparison,
	@JsonProperty("reviews") List<Review> reviews
) {

	public record MainProduct(
		@JsonProperty("id") UUID id,
		@JsonProperty("name") String name,
		@JsonProperty("brand") String brand,
		@JsonProperty("imageUrl") String imageUrl,
		@JsonProperty("lowestPrice") String lowestPrice,
		@JsonProperty("lowestPriceStore") String lowestPriceStore,
		@JsonProperty("totalProductCount") Integer totalProductCount,
		@JsonProperty("category") Category category
	) {
	}

	public record Category(
		@JsonProperty("large") CategoryItem large,
		@JsonProperty("medium") CategoryItem medium
	) {
	}

	public record CategoryItem(
		@JsonProperty("id") UUID id,
		@JsonProperty("name") String name
	) {
	}

	public record PriceComparison(
		@JsonProperty("totalCount") Integer totalCount,
		@JsonProperty("platformLowestPrices") List<PriceItem> platformLowestPrices,
		@JsonProperty("allProducts") List<PriceItem> allProducts
	) {
	}

	public record PriceItem(
		@JsonProperty("id") UUID id,
		@JsonProperty("platformType") String platformType,
		@JsonProperty("platformName") String platformName,
		@JsonProperty("productName") String productName,
		@JsonProperty("price") String price,
		@JsonProperty("shippingFee") String shippingFee,
		@JsonProperty("totalPrice") String totalPrice,
		@JsonProperty("saleLink") String saleLink,
		@JsonProperty("isLowest") Boolean isLowest
	) {
	}

	public record Review(
		@JsonProperty("reviewId") UUID reviewId,
		@JsonProperty("title") String title,
		@JsonProperty("content") String content,
		@JsonProperty("rating") BigDecimal rating,
		@JsonProperty("platformType") String platformType,
		@JsonProperty("images") List<String> images,
		@JsonProperty("authorName") String authorName,
		@JsonProperty("createdAt") LocalDateTime createdAt
	) {
	}
}
