package com.sparta.productservice.product.app;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.common.dto.PageSizeType;
import com.sparta.productservice.product.app.dto.GetProductResult;
import com.sparta.productservice.product.app.service.CategoryClient;
import com.sparta.productservice.product.app.service.ReviewClient;
import com.sparta.productservice.product.app.service.dto.CategoryInfo;
import com.sparta.productservice.product.app.service.dto.ReviewInfo;
import com.sparta.productservice.product.domain.entity.MainProduct;
import com.sparta.productservice.product.domain.repository.MainProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
	private final MainProductRepository mainProductRepository;
	private final CategoryClient categoryClient;
	private final ReviewClient reviewClient;

	public GetProductResult getProductDetailById(UUID mainProductId) {
		MainProduct mainProduct = mainProductRepository.getById(mainProductId)
			.orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
		CategoryInfo categoryInfo = categoryClient.getCategoryByMediumId(mainProduct.getCategoryMediumId());

		List<ReviewInfo> reviewInfos = reviewClient.getReviewsByMainProductId(mainProductId, PageSizeType.SIZE_2);
		return GetProductResult.from(mainProduct, categoryInfo, reviewInfos);
	}
}
