package com.sparta.productservice.review.present;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.productservice.common.dto.ApiResponse;
import com.sparta.productservice.common.dto.PageRequest;
import com.sparta.productservice.review.app.ReviewService;
import com.sparta.productservice.review.app.command.ListReviewCommand;
import com.sparta.productservice.review.app.dto.ListReviewResult;
import com.sparta.productservice.review.present.dto.ListReviewResponse;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {
	private final ReviewService reviewService;

	@GetMapping
	public ResponseEntity<ApiResponse<ListReviewResponse>> getReviewsByMainProductId(
		@RequestParam @NotNull UUID mainProductId,
		@ModelAttribute PageRequest pageRequest
	) {
		ListReviewCommand command = new ListReviewCommand(mainProductId, pageRequest.toPageable());
		ListReviewResult reviews = reviewService.getReviewsByMainProductId(command);

		ListReviewResponse response = ListReviewResponse.from(reviews);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}