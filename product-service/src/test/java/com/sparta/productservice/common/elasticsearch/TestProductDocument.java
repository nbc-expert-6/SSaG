package com.sparta.productservice.common.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "test-products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestProductDocument {
	@Id
	private String id;
	@Field(type = FieldType.Text)
	private String name;
	@Field(type = FieldType.Integer)
	private Integer price;
}
