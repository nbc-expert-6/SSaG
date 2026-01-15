package com.sparta.productservice.product.app.command;

import java.math.BigDecimal;
import java.util.UUID;

import com.sparta.productservice.product.domain.entity.Product;
import com.sparta.productservice.product.domain.vo.PlatformType;
import com.sparta.productservice.product.domain.vo.ProductLink;
import com.sparta.productservice.product.domain.vo.ProductPrice;

public record CreateProductCommand(
	UUID mainProductId,
	String name,
	PlatformType platformType,
	String saleLink,
	BigDecimal price,
	BigDecimal shippingFee
) {
	public Product toProduct() {
		ProductLink link = ProductLink.builder()
			.platformType(platformType)
			.url(saleLink)
			.build();
		ProductPrice productPrice = ProductPrice.builder()
			.price(price)
			.shippingFee(shippingFee)
			.build();

		return Product.builder()
			.name(this.name)
			.productLink(link)
			.productPrice(productPrice)
			.build();
	}
}
