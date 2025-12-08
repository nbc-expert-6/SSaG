package com.sparta.recommendservice.domain.product_vector.infrastructure.external;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.sparta.recommendservice.domain.product_vector.infrastructure.external.dto.ProductInfoFeignClientResponse;
import com.sparta.recommendservice.domain.product_vector.presentation.common.dto.ApiResponse;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductFeignClient {

	@GetMapping("/api/v1/main-products/{productId}")
	ApiResponse<ProductInfoFeignClientResponse> getProductInfo(@PathVariable UUID productId);

}
