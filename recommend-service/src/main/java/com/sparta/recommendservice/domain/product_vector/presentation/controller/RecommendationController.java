package com.sparta.recommendservice.domain.product_vector.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.recommendservice.domain.product_vector.application.service.ProductVectorService;
import com.sparta.recommendservice.domain.product_vector.presentation.common.dto.ApiResponse;
import com.sparta.recommendservice.domain.product_vector.presentation.common.dto.RecommendationViewResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendation")
public class RecommendationController {

	private final ProductVectorService service;

	@GetMapping("/{productId}")
	public ResponseEntity<ApiResponse<List<RecommendationViewResponseDto>>> getRecommendations(
		@PathVariable("productId") UUID productId) {

		ApiResponse<List<RecommendationViewResponseDto>> recommendations = service.getRecommendations(productId);
		return ResponseEntity.status(HttpStatus.OK).body(recommendations);
	}

}
