package com.sparta.productservice.product.present;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.productservice.category.app.CategoryService;
import com.sparta.productservice.category.app.dto.CategoryLargeResult;
import com.sparta.productservice.category.present.dto.ListCategoryResponse;
import com.sparta.productservice.common.dto.ApiResponse;
import com.sparta.productservice.product.app.ProductService;
import com.sparta.productservice.product.app.dto.GetProductResult;
import com.sparta.productservice.product.present.dto.GetProductResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
	private final ProductService productService;

	@GetMapping("/{mainProductId}")
	public ResponseEntity<ApiResponse<GetProductResponse>> getAllCategories(
		@PathVariable UUID mainProductId
	) {
		GetProductResult productResult = productService.getProductDetailById(mainProductId);

		GetProductResponse response = GetProductResponse.from(productResult);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
