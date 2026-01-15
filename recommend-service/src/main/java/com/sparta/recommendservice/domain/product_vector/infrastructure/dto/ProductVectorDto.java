package com.sparta.recommendservice.domain.product_vector.infrastructure.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVectorDto {

	private UUID productId;
	private float[] embedding;

}