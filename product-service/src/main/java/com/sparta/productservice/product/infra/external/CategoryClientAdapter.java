package com.sparta.productservice.product.infra.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sparta.productservice.common.dto.ApiResponse;
import com.sparta.productservice.product.app.port.out.CategoryClient;
import com.sparta.productservice.product.app.port.out.dto.CategoryInfo;
import com.sparta.productservice.product.infra.external.dto.FeignGetCategoryResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryClientAdapter implements CategoryClient {
	private final CategoryFeignClient categoryFeignClient;

	@Override
	public CategoryInfo getCategoryByMediumId(UUID id) {
		ApiResponse<FeignGetCategoryResponse> response = categoryFeignClient.getCategoryByMediumCategoryId(id);
		if (!response.success()) {
			log.warn("카테고리 상세 조회 실패 - message: {}", response.message());
			throw new IllegalArgumentException(response.message());
		}
		return response.data().toCategoryInfo();
	}
}
