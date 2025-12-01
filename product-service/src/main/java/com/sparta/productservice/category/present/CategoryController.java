package com.sparta.productservice.category.present;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.productservice.category.app.dto.CategoryLargeResult;
import com.sparta.productservice.category.app.service.CategoryService;
import com.sparta.productservice.category.present.dto.ListCategoryResponse;
import com.sparta.productservice.common.dto.ApiResponse;

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
}
