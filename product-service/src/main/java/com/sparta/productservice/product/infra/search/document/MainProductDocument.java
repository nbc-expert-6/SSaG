package com.sparta.productservice.product.infra.search.document;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(indexName = "main_products")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainProductDocument {

	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@Field(type = FieldType.Text, analyzer = "nori")
	private String name;

	@Field(type = FieldType.Keyword)
	private String brand;

	@Field(type = FieldType.Keyword)
	private String imageUrl;

	@Field(type = FieldType.Double)
	private BigDecimal lowestPrice;

	@Field(type = FieldType.Integer)
	private Integer productCount;

	@Field(type = FieldType.Double)
	private BigDecimal rating;

	@Field(type = FieldType.Integer)
	private Long reviewCount;

	@Field(type = FieldType.Long)
	private Long clickCount;

	@Field(type = FieldType.Date)
	private Instant createdAt;

	@Field(type = FieldType.Keyword)
	private String categoryId;

	@Field(type = FieldType.Boolean)
	private Boolean deleted;
}
