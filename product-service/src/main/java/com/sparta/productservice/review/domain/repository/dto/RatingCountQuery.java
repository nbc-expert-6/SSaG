package com.sparta.productservice.review.domain.repository.dto;

public record RatingCountQuery(
	Integer rating,
	Long count
) {
}