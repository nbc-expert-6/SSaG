package com.sparta.productservice.product.app;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.product.app.dto.GetProductResult;
import com.sparta.productservice.product.app.service.CategoryClient;
import com.sparta.productservice.product.app.service.dto.CategoryInfo;
import com.sparta.productservice.product.domain.entity.MainProduct;
import com.sparta.productservice.product.domain.repository.MainProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
	private final MainProductRepository mainProductRepository;
	private final CategoryClient categoryClient;

	public GetProductResult getProductDetailById(UUID mainProductId) {
		MainProduct mainProduct = mainProductRepository.getById(mainProductId)
			.orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
		CategoryInfo categoryInfo = categoryClient.getCategoryByMediumId(mainProduct.getCategoryMediumId());

		//TODO: 리뷰 기능 구현후 리뷰 관련 정보 추가
		return GetProductResult.from(mainProduct, categoryInfo);
	}

}
