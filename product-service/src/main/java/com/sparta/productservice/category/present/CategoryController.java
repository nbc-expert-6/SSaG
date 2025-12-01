package com.sparta.productservice.category.present;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.productservice.category.app.dto.CategoryLargeResult;
import com.sparta.productservice.category.app.CategoryService;
import com.sparta.productservice.category.app.dto.CategoryMediumResult;
import com.sparta.productservice.category.present.dto.GetCategoryResponse;
import com.sparta.productservice.category.present.dto.ListCategoryResponse;
import com.sparta.productservice.common.dto.ApiResponse;
import com.sparta.productservice.product.app.dto.GetProductResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {
	private final CategoryService categoryService;

	@GetMapping
	public ResponseEntity<ApiResponse<ListCategoryResponse>> getAllCategories() {
		List<CategoryLargeResult> categories = categoryService.getAllCategories();

		ListCategoryResponse response = ListCategoryResponse.from(categories);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/medium/{mediumCategoryId}")
	public ResponseEntity<ApiResponse<GetCategoryResponse>> getCategoryById(
		@PathVariable UUID mediumCategoryId
	) {
		CategoryMediumResult category = categoryService.getCategoryMediumById(mediumCategoryId);

		GetCategoryResponse response = GetCategoryResponse.from(category);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
