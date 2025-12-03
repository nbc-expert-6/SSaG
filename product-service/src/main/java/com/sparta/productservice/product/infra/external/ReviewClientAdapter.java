package com.sparta.productservice.product.infra.external;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sparta.productservice.common.dto.ApiResponse;
import com.sparta.productservice.common.dto.PageSizeType;
import com.sparta.productservice.product.app.port.out.ReviewClient;
import com.sparta.productservice.product.app.port.out.dto.ReviewInfo;
import com.sparta.productservice.product.infra.external.dto.FeignListReviewResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewClientAdapter implements ReviewClient {
	private final ReviewFeignClient reviewFeignClient;

	@Override
	public List<ReviewInfo> getReviewsByMainProductId (UUID mainProductId, PageSizeType pageSize) {
		ApiResponse<FeignListReviewResponse> response = reviewFeignClient.getReviewByMainProductId(mainProductId,
			pageSize);
		if (!response.success()) {
			log.info("리뷰 상세 조회 실패 - message: {}", response.message());
			throw new IllegalArgumentException(response.message());
		}

		return response.data().toReviewInfoList();
	}
}
