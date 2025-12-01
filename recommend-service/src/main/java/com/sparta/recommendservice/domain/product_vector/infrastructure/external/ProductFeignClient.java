package com.sparta.recommendservice.domain.product_vector.infrastructure.external;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sparta.recommendservice.domain.product_vector.infrastructure.external.dto.ProductInfoFeignClientResponse;
import com.sparta.recommendservice.domain.product_vector.presentation.common.dto.ApiResponse;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductFeignClient {

	@GetMapping("/api/v1/products/{product_id}")
	ApiResponse<ProductInfoFeignClientResponse> getProductInfo(@RequestParam("productId") UUID productId);

}
