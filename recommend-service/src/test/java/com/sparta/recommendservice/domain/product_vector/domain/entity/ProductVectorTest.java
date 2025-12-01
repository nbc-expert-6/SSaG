package com.sparta.recommendservice.domain.product_vector.domain.entity;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pgvector.PGvector;

class ProductVectorTest {

	private ProductVector createProductVector(float[] arr) {
		return ProductVector.builder()
			.productId(UUID.randomUUID())
			.embedding(new PGvector(arr))
			.metadata("{}")
			.build();
	}

	@Test
	@DisplayName("동일 벡터 비교 시 코사인 유사도는 1.0")
	void testCosineDistance_SameVector() {
		float[] arr = new float[] {1.0f, 2.0f, 3.0f};
		ProductVector vectorA = createProductVector(arr);
		ProductVector vectorB = createProductVector(arr);

		double similarity = vectorA.cosineDistance(vectorB);

		assertThat(similarity).isEqualTo(1.0);
	}

	@Test
	@DisplayName("직교 벡터 비교 시 코사인 유사도는 0.0")
	void testCosineDistance_OrthogonalVector() {
		float[] arrA = new float[] {1.0f, 0.0f, 0.0f};
		float[] arrB = new float[] {0.0f, 1.0f, 0.0f};
		ProductVector vectorA = createProductVector(arrA);
		ProductVector vectorB = createProductVector(arrB);

		double similarity = vectorA.cosineDistance(vectorB);

		assertThat(similarity).isEqualTo(0.0);
	}

	@Test
	@DisplayName("영 벡터 포함 시 코사인 유사도는 0.0")
	void testCosineDistance_ZeroVector() {
		float[] arrA = new float[] {0.0f, 0.0f, 0.0f};
		float[] arrB = new float[] {1.0f, 2.0f, 3.0f};
		ProductVector vectorA = createProductVector(arrA);
		ProductVector vectorB = createProductVector(arrB);

		double similarity = vectorA.cosineDistance(vectorB);

		assertThat(similarity).isEqualTo(0.0);
	}

	@Test
	@DisplayName("벡터 길이가 다르면 IllegalStateException 발생")
	void testCosineDistance_DifferentLength() {
		float[] arrA = new float[] {1.0f, 2.0f};
		float[] arrB = new float[] {1.0f, 2.0f, 3.0f};
		ProductVector vectorA = createProductVector(arrA);
		ProductVector vectorB = createProductVector(arrB);

		assertThrows(IllegalStateException.class, () -> vectorA.cosineDistance(vectorB));
	}

}