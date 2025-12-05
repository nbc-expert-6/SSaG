package com.sparta.productservice.product.infra.event.message;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.productservice.product.app.command.CreateProductCommand;
import com.sparta.productservice.product.domain.vo.PlatformType;

public record CrawledProductMessage(
	String mainProductId,
	String platform,
	String saleLink,
	String brand,
	String name,
	String seller,
	BigDecimal price,
	BigDecimal shippingFee,
	String imageUrl
) {
	public CreateProductCommand toCommand() {
		return new CreateProductCommand(
			UUID.fromString(this.mainProductId),
			this.name,
			PlatformType.valueOf(this.platform.toUpperCase()),  // 대소문자 안전 처리
			this.saleLink,
			this.price,
			this.shippingFee
		);
	}
}
