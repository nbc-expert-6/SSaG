package com.sparta.productservice.product.infra.event.message;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.productservice.product.app.command.CreateProductCommand;
import com.sparta.productservice.product.domain.vo.PlatformType;

public record CrawledProductMessage(
	@JsonProperty("main_product_id") String mainProductId,
	@JsonProperty("platform") String platform,
	@JsonProperty("sale_link") String saleLink,
	@JsonProperty("brand") String brand,
	@JsonProperty("name") String name,
	@JsonProperty("seller") String seller,
	@JsonProperty("price") BigDecimal price,
	@JsonProperty("shipping_fee") BigDecimal shippingFee,
	@JsonProperty("image_url") String imageUrl
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
