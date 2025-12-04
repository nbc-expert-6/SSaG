package com.sparta.productservice.product.app;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.common.dto.PageSizeType;
import com.sparta.productservice.product.app.command.CreateProductCommand;
import com.sparta.productservice.product.app.dto.GetProductResult;
import com.sparta.productservice.product.app.port.in.CreateProductUseCase;
import com.sparta.productservice.product.app.port.out.CategoryClient;
import com.sparta.productservice.product.app.port.out.ReviewClient;
import com.sparta.productservice.product.app.port.out.dto.CategoryInfo;
import com.sparta.productservice.product.app.port.out.dto.ReviewInfo;
import com.sparta.productservice.product.domain.entity.MainProduct;
import com.sparta.productservice.product.domain.repository.MainProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService implements CreateProductUseCase {
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

	/**
	 * 현재는 등록된 메인상품이 있어야 하위상품을 등록 할 수 있습니다.
	 * @param command
	 */
	@Override
	@Transactional
	public void createProduct(CreateProductCommand command) {
		MainProduct mainProduct = mainProductRepository.getById(command.mainProductId())
			.orElseThrow(() -> new NoSuchElementException("메인상품이 등록되어있지 않습니다."));

		mainProduct.addProduct(command.toProduct());
	}
}
