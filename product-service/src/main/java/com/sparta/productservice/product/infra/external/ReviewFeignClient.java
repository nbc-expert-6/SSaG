package com.sparta.productservice.product.infra.external;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sparta.productservice.common.dto.ApiResponse;
import com.sparta.productservice.common.dto.PageSizeType;
import com.sparta.productservice.product.infra.external.dto.FeignListReviewResponse;

@FeignClient(
	name = "review-feign-client",
	contextId = "reviewFeignClient",
	url = "${product-service.url}"
)
public interface ReviewFeignClient {
	@GetMapping("/api/v1/reviews")
	ApiResponse<FeignListReviewResponse> getReviewByMainProductId(
		@RequestParam("mainProductId") UUID mainProductId,
		@RequestParam("size") PageSizeType size);
}
