package com.sparta.productservice.product.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sparta.productservice.product.app.port.out.dto.CategoryInfo;
import com.sparta.productservice.product.app.port.out.dto.ReviewInfo;
import com.sparta.productservice.product.domain.entity.MainProduct;
import com.sparta.productservice.product.domain.entity.Product;
import com.sparta.productservice.product.domain.vo.PlatformType;

public record GetProductResult(
	MainProductDto mainProduct,
	PriceComparisonDto priceComparison,
	List<ReviewDto> reviews
) {
	public static GetProductResult from(MainProduct mainProduct, CategoryInfo categoryInfo, List<ReviewInfo> reviewInfos) {
		BigDecimal lowestPrice = mainProduct.getLowestPrice();

		Map<PlatformType, Product> platformLowestMap = mainProduct.getProducts().stream()
			.collect(Collectors.toMap(
				Product::getPlatformType,
				Function.identity(),
				(p1, p2) -> p1.getPrice().compareTo(p2.getPrice()) < 0 ? p1 : p2
			));

		return new GetProductResult(
			MainProductDto.from(mainProduct, categoryInfo),
			PriceComparisonDto.from(mainProduct.getProducts(), platformLowestMap, lowestPrice),
			reviewInfos.stream().map(r -> ReviewDto.from(r)).toList()
		);
	}

	public record MainProductDto(
		UUID id,
		String name,
		String brand,
		String imageUrl,
		BigDecimal lowestPrice,
		PlatformType lowestPriceStore,
		Integer totalProductCount,
		CategoryDto category
	) {
		public static MainProductDto from(MainProduct mainProduct, CategoryInfo categoryInfo) {
			PlatformType lowestPriceStore = mainProduct.getProducts().stream()
				.min(Comparator.comparing(Product::getPrice))
				.map(Product::getPlatformType)
				.orElse(null);

			return new MainProductDto(
				mainProduct.getId(),
				mainProduct.getName(),
				mainProduct.getBrand(),
				mainProduct.getImageUrl(),
				mainProduct.getLowestPrice(),
				lowestPriceStore,
				mainProduct.getProducts().size(),
				CategoryDto.from(categoryInfo)
			);
		}
	}

	public record CategoryDto(
		UUID largeId,
		String largeName,
		UUID mediumId,
		String mediumName
	) {
		public static CategoryDto from(CategoryInfo categoryInfo) {
			return new CategoryDto(
				categoryInfo.largeCategoryId(),
				categoryInfo.largeCategoryName(),
				categoryInfo.mediumCategoryId(),
				categoryInfo.mediumCategoryName()
			);
		}
	}

	public record PriceComparisonDto(
		Integer totalCount,
		List<ProductDto> platformLowestPrices,
		List<ProductDto> allProducts
	) {
		public static PriceComparisonDto from(
			List<Product> products,
			Map<PlatformType, Product> platformLowestMap,
			BigDecimal globalLowestPrice
		) {
			List<ProductDto> platformLowestPrices = platformLowestMap.values().stream()
				.map(p -> ProductDto.from(p, globalLowestPrice))
				.sorted(Comparator.comparing(dto -> dto.totalPrice()))
				.toList();

			List<ProductDto> allProducts = products.stream()
				.map(p -> ProductDto.from(p, globalLowestPrice))
				.sorted(Comparator.comparing(dto -> dto.totalPrice()))
				.toList();

			return new PriceComparisonDto(
				products.size(),
				platformLowestPrices,
				allProducts
			);
		}
	}

	public record ProductDto(
		UUID id,
		PlatformType platformType,
		BigDecimal price,
		BigDecimal shippingFee,
		Boolean isFreeShipping,
		BigDecimal totalPrice,
		String saleLink,
		Boolean isLowest
	) {
		public static ProductDto from(Product product, BigDecimal globalLowestPrice) {
			return new ProductDto(
				product.getId(),
				product.getPlatformType(),
				product.getPrice(),
				product.getShippingFee(),
				product.isFreeShipping(),
				product.getPrice(),
				product.getLink(),
				product.getPrice().compareTo(globalLowestPrice) == 0
			);
		}
	}

	public record ReviewDto(
		UUID id,
		String title,
		String content,
		BigDecimal rating,
		PlatformType platformType,
		List<String> images,
		String authorName,
		LocalDateTime createdAt
	) {
		public static ReviewDto from(ReviewInfo reviewInfo) {
			return new ReviewDto(
				reviewInfo.reviewId(),
				reviewInfo.title(),
				reviewInfo.content(),
				reviewInfo.rating(),
				reviewInfo.platformType(),
				reviewInfo.images(),
				reviewInfo.authorName(),
				reviewInfo.createdAt()
			);
		}
	}
}