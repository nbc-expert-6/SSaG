package com.sparta.recommendservice.domain.product_vector.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import com.sparta.recommendservice.domain.product_vector.domain.entity.ProductVector;
import com.sparta.recommendservice.domain.product_vector.domain.repository.ProductVectorRepository;

@ExtendWith(MockitoExtension.class)
class ProductVectorServiceTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Mock
	private PythonRunner pythonRunner;

	@Mock
	private ProductVectorRepository productVectorRepository;

	@InjectMocks
	private ProductVectorService service;

	private ProductVector createProductVector(UUID id, float[] embeddingArr, Map<String, Object> metaMap) {
		try {
			String metadataJson = MAPPER.writeValueAsString(metaMap);
			return ProductVector.builder()
				.productId(id)
				.embedding(new PGvector(embeddingArr))
				.metadata(metadataJson)
				.build();
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("updateProductVectors() - PythonRunner 정상 호출")
	void testUpdateProductVectors_Success() throws IOException, InterruptedException {
		doNothing().when(pythonRunner).run();

		service.updateProductVectors();

		verify(pythonRunner, times(1)).run();
	}

	@Test
	@DisplayName("updateProductVectors() - PythonRunner 예외 발생 시 RuntimeException 발생")
	void testUpdateProductVectors_Failure() throws IOException, InterruptedException {
		doThrow(new IOException("fail")).when(pythonRunner).run();

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateProductVectors());

		assertThat(ex).hasMessageContaining("Vector update failed!");
		verify(pythonRunner, times(1)).run();
	}

	@Test
	@DisplayName("recommend() - 후보 상품 전체 검증")
	void testRecommend() {
		UUID targetId = UUID.randomUUID();
		Map<String, Object> targetMeta = Map.of(
			"brand", "BrandA",
			"categoryId", "Cat1",
			"price", 100.0
		);

		ProductVector target = createProductVector(targetId, new float[] {1.0f, 0.0f}, targetMeta);

		ProductVector candidate1 = createProductVector(UUID.randomUUID(), new float[] {1.0f, 0.0f}, Map.of(
			"brand", "BrandA",
			"categoryId", "Cat1",
			"price", 105.0
		));
		ProductVector candidate2 = createProductVector(UUID.randomUUID(), new float[] {0.0f, 1.0f}, Map.of(
			"brand", "BrandB",
			"categoryId", "Cat2",
			"price", 95.0
		));

		List<ProductVector> candidates = List.of(candidate1, candidate2);

		when(productVectorRepository.findTopKByEmbedding(any(PGvector.class), eq(10)))
			.thenReturn(candidates);

		List<UUID> result = service.recommend(target, 10, 2);

		verify(productVectorRepository, times(1))
			.findTopKByEmbedding(any(PGvector.class), eq(10));

		assertTrue(result.contains(candidate1.getProductId()), "Result should contain candidate1 UUID");
		assertTrue(result.contains(candidate2.getProductId()), "Result should contain candidate2 UUID");

		assertEquals(2, result.size(), "Result size should match number of candidates");
	}

	@Test
	void testParseMetadataUsingReflection() throws Exception {
		String json = "{\"brand\":\"BrandA\",\"categoryId\":\"Cat1\",\"price\":100.0}";

		Method method = ProductVectorService.class.getDeclaredMethod("parseMetadata", String.class);
		method.setAccessible(true); // private 접근 허용

		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>)method.invoke(service, json);

		assertEquals("BrandA", result.get("brand"));
		assertEquals("Cat1", result.get("categoryId"));
		assertEquals(100.0, result.get("price"));
	}

	@Test
	@DisplayName("parseMetadata() - 잘못된 JSON 문자열일 경우 RuntimeException 발생")
	void testParseMetadata_Failure() throws Exception {
		String invalidJson = "{brand:BrandA"; // 잘못된 JSON

		// private 메서드 접근 허용
		Method method = ProductVectorService.class.getDeclaredMethod("parseMetadata", String.class);
		method.setAccessible(true);

		RuntimeException ex = assertThrows(RuntimeException.class,
			() -> {
				try {
					method.invoke(service, invalidJson);
				} catch (InvocationTargetException e) {
					// 실제 예외를 RuntimeException으로 꺼내기
					throw (RuntimeException)e.getTargetException();
				}
			});

		assertTrue(ex.getMessage().contains("Failed to parse metadata"));
	}

}
