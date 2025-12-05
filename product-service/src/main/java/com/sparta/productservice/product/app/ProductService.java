package com.sparta.productservice.product.app;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.common.dto.PageSizeType;
import com.sparta.productservice.product.app.command.CreateMainProductCommand;
import com.sparta.productservice.product.app.command.CreateProductCommand;
import com.sparta.productservice.product.app.command.SearchMainProductCommand;
import com.sparta.productservice.product.app.dto.CreateMainProductResult;
import com.sparta.productservice.product.app.dto.GetProductResult;
import com.sparta.productservice.product.app.port.in.CreateProductUseCase;
import com.sparta.productservice.product.app.port.out.CategoryClient;
import com.sparta.productservice.product.app.port.out.ReviewClient;
import com.sparta.productservice.product.app.port.out.dto.CategoryInfo;
import com.sparta.productservice.product.app.port.out.dto.ReviewInfo;
import com.sparta.productservice.product.domain.entity.MainProduct;
import com.sparta.productservice.product.domain.event.ProductCreatedEvent;
import com.sparta.productservice.product.domain.repository.MainProductRepository;
import com.sparta.productservice.product.domain.repository.MainProductSearchRepository;
import com.sparta.productservice.product.domain.repository.dto.MainProductSearchResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService implements CreateProductUseCase {
	private final MainProductRepository mainProductRepository;
	private final CategoryClient categoryClient;
	private final ReviewClient reviewClient;
	private final MainProductSearchRepository mainProductSearchRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public GetProductResult getProductDetailById(UUID mainProductId) {
		MainProduct mainProduct = mainProductRepository.getById(mainProductId)
			.orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
		CategoryInfo categoryInfo = categoryClient.getCategoryByMediumId(mainProduct.getCategoryMediumId());
		mainProductRepository.increaseClick(mainProductId);

		List<ReviewInfo> reviewInfos = reviewClient.getReviewsByMainProductId(mainProductId, PageSizeType.SIZE_2);
		return GetProductResult.from(mainProduct, categoryInfo, reviewInfos);
	}

	/**
	 * 현재는 등록된 메인상품이 있어야 하위상품을 등록 할 수 있습니다
	 * @param command
	 */
	@Override
	@Transactional
	public void createProduct(CreateProductCommand command) {
		MainProduct mainProduct = mainProductRepository.getById(command.mainProductId())
			.orElseThrow(() -> new NoSuchElementException("메인상품이 등록되어있지 않습니다."));

		mainProduct.addProduct(command.toProduct());

		eventPublisher.publishEvent(ProductCreatedEvent.from(mainProduct));
	}

	@Transactional
	public CreateMainProductResult createMainProduct(CreateMainProductCommand command) {
		MainProduct mainProduct = command.toMainProduct();
		MainProduct saved = mainProductRepository.save(mainProduct);

		return CreateMainProductResult.from(saved);
	}

	@Transactional(readOnly = true)
	public Page<MainProductSearchResult> searchMainProduct(SearchMainProductCommand command) {
		return mainProductSearchRepository.search(command);
	}
}
