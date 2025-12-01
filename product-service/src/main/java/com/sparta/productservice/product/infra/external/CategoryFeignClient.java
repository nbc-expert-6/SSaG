package com.sparta.productservice.product.infra.external;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.sparta.productservice.common.dto.ApiResponse;
import com.sparta.productservice.product.app.service.dto.CategoryInfo;
import com.sparta.productservice.product.infra.external.dto.FeignGetCategoryResponse;

@FeignClient(
	name = "category-feign-client",
	contextId = "categoryFeignClient",
	url = "${product-service.url}"
)
public interface CategoryFeignClient {
	@GetMapping("/api/v1/categories/medium/{medium_category_id}")
	ApiResponse<FeignGetCategoryResponse> getCategoryByMediumCategoryId(
		@PathVariable("medium_category_id") UUID mediumCategoryId);
}
