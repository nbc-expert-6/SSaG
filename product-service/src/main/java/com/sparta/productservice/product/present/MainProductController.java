package com.sparta.productservice.product.present;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.productservice.common.dto.ApiResponse;
import com.sparta.productservice.product.app.ProductService;
import com.sparta.productservice.product.app.dto.CreateMainProductResult;
import com.sparta.productservice.product.app.dto.GetProductResult;
import com.sparta.productservice.product.domain.repository.dto.MainProductSearchResult;
import com.sparta.productservice.product.present.dto.CreateMainProductRequest;
import com.sparta.productservice.product.present.dto.CreateMainProductResponse;
import com.sparta.productservice.product.present.dto.GetProductResponse;
import com.sparta.productservice.product.present.dto.SearchMainProductRequest;
import com.sparta.productservice.product.present.dto.SearchMainProductResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/main-products")
@RequiredArgsConstructor
@Validated
public class MainProductController {
	private final ProductService productService;

	@GetMapping("/{mainProductId}")
	public ResponseEntity<ApiResponse<GetProductResponse>> getMainProductWithProducts(
		@PathVariable UUID mainProductId
	) {
		GetProductResult productResult = productService.getProductDetailById(mainProductId);

		GetProductResponse response = GetProductResponse.from(productResult);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<CreateMainProductResponse>> createMainProduct(
		@RequestBody @Valid CreateMainProductRequest request) {
		CreateMainProductResult result = productService.createMainProduct(request.toCommand());
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(
				ApiResponse.success(
					CreateMainProductResponse.from(result),
					"메인 상품 생성 성공"
				)
			);
	}

	@PostMapping("/search")
	public ResponseEntity<ApiResponse<SearchMainProductResponse>> searchMainProduct(
		@RequestBody @Valid SearchMainProductRequest request
	) {
		Page<MainProductSearchResult> result = productService.searchMainProduct(request.toCommand());
		SearchMainProductResponse response = SearchMainProductResponse.from(result);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/sync")
	public Void syncEs() {
		productService.syncAllFromRdbToEs();
		return null;
	}
}
