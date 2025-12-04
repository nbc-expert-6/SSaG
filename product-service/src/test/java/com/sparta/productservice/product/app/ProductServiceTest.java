package com.sparta.productservice.product.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.productservice.product.app.command.CreateMainProductCommand;
import com.sparta.productservice.product.app.dto.CreateMainProductResult;
import com.sparta.productservice.product.domain.entity.MainProduct;
import com.sparta.productservice.product.domain.repository.MainProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private MainProductRepository mainProductRepository;

	@InjectMocks
	private ProductService mainProductService;

	@Test
	void createMainProduct_success() {
		// given
		CreateMainProductCommand command = new CreateMainProductCommand(
			UUID.randomUUID(),
			"테스트 상품",
			BigDecimal.valueOf(10000),
			"https://test.com/img.jpg",
			"Nike"
		);

		MainProduct mockProduct = command.toMainProduct();
		ReflectionTestUtils.setField(mockProduct, "id", UUID.randomUUID());

		when(mainProductRepository.save(any(MainProduct.class)))
			.thenReturn(mockProduct);

		// when
		CreateMainProductResult result = mainProductService.createMainProduct(command);

		// then
		System.out.println("\n=========== 테스트 출력 ===========");
		System.out.println("생성된 상품 ID   : " + result.id());
		System.out.println("상품 이름       : " + result.name());
		System.out.println("==================================\n");

		// then
		assertNotNull(result.id());
		assertEquals(command.name(), result.name());
		verify(mainProductRepository, times(1)).save(any(MainProduct.class));
	}

}