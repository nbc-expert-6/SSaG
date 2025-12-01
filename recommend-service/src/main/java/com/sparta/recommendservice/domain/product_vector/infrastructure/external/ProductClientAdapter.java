package com.sparta.recommendservice.domain.product_vector.infrastructure.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sparta.recommendservice.domain.product_vector.application.service.ProductClient;
import com.sparta.recommendservice.domain.product_vector.application.service.dto.ProductInfoDto;
import com.sparta.recommendservice.domain.product_vector.infrastructure.external.dto.ProductInfoFeignClientResponse;
import com.sparta.recommendservice.domain.product_vector.presentation.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductClient {

	private final ProductFeignClient productFeignClient;

	@Override
	public ProductInfoDto getProductInfo(UUID productId) {
		ApiResponse<ProductInfoFeignClientResponse> response = productFeignClient.getProductInfo(productId);

		if (!response.success() || response.data() == null) {
			log.error("상품 정보 조회 실패 : {}", response.message());
			throw new IllegalArgumentException(response.message());
		}

		ProductInfoFeignClientResponse feignDto = response.data();

		return new ProductInfoDto(
			feignDto.productId(),
			feignDto.brand(),
			feignDto.categoryMediumId(),
			feignDto.price()
		);
	}

}
