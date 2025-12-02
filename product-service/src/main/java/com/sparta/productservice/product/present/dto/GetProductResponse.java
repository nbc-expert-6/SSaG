package com.sparta.productservice.product.present.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.sparta.productservice.product.app.dto.GetProductResult;

public record GetProductResponse(
	MainProductDto mainProduct,
	PriceComparisonDto priceComparison
) {
	public record MainProductDto(
		UUID id,
		String name,
		String brand,
		String imageUrl,
		String lowestPrice,
		String lowestPriceStore,
		Integer totalProductCount,
		CategoryDto category
	) {}

	public record CategoryDto(
		LargeCategoryDto large,
		MediumCategoryDto medium
	) {
		public record LargeCategoryDto(
			String id,
			String name
		) {}

		public record MediumCategoryDto(
			String id,
			String name
		) {}
	}

	public record PriceComparisonDto(
		Integer totalCount,
		List<PlatformPriceDto> platformLowestPrices,
		List<ProductPriceDto> allProducts
	) {}

	public record PlatformPriceDto(
		String platformType,
		String platformName,
		String price,
		String shippingFee,
		String totalPrice,
		String saleLink,
		Boolean isGlobalLowest,
		String badge
	) {}

	public record ProductPriceDto(
		String id,
		String platformType,
		String platformName,
		String productName,
		String price,
		String shippingFee,
		String totalPrice,
		String saleLink,
		Boolean isLowest
	) {}

	// Result -> Response 변환
	public static GetProductResponse from(GetProductResult result) {
		return new GetProductResponse(
			convertMainProduct(result.mainProduct()),
			convertPriceComparison(result.priceComparison())
		);
	}

	private static MainProductDto convertMainProduct(GetProductResult.MainProductDto dto) {
		return new MainProductDto(
			dto.id(),
			dto.name(),
			dto.brand(),
			dto.imageUrl(),
			formatPrice(dto.lowestPrice()),
			dto.lowestPriceStore().getDescription(),
			dto.totalProductCount(),
			convertCategory(dto.category())
		);
	}

	private static CategoryDto convertCategory(GetProductResult.CategoryDto categoryDto) {
		return new CategoryDto(
			new CategoryDto.LargeCategoryDto(
				categoryDto.largeId().toString(),
				categoryDto.largeName()
			),
			new CategoryDto.MediumCategoryDto(
				categoryDto.mediumId().toString(),
				categoryDto.mediumName()
			)
		);
	}

	private static PriceComparisonDto convertPriceComparison(GetProductResult.PriceComparisonDto dto) {
		return new PriceComparisonDto(
			dto.totalCount(),
			dto.platformLowestPrices().stream()
				.map(GetProductResponse::convertToPlatformPrice)
				.toList(),
			dto.allProducts().stream()
				.map(GetProductResponse::convertToProductPrice)
				.toList()
		);
	}

	private static PlatformPriceDto convertToPlatformPrice(GetProductResult.ProductDto dto) {
		return new PlatformPriceDto(
			dto.platformType().name(),
			dto.platformType().getDescription(),
			formatPrice(dto.price()),
			formatShippingFee(dto.shippingFee()),
			formatPrice(dto.totalPrice()),
			dto.saleLink(),
			dto.isLowest(),
			dto.isLowest() ? "최저가" : null
		);
	}

	private static ProductPriceDto convertToProductPrice(GetProductResult.ProductDto dto) {
		return new ProductPriceDto(
			dto.id().toString(),
			dto.platformType().name(),
			dto.platformType().getDescription(),
			dto.platformType().getDescription() + " 상품",
			formatPrice(dto.price()),
			formatShippingFee(dto.shippingFee()),
			formatPrice(dto.totalPrice()),
			dto.saleLink(),
			dto.isLowest()
		);
	}

	private static String formatPrice(BigDecimal price) {
		return String.format("%,d원", price.intValue());
	}

	private static String formatShippingFee(BigDecimal fee) {
		return fee.compareTo(BigDecimal.ZERO) == 0
			? "무료배송"
			: String.format("%,d원", fee.intValue());
	}
}