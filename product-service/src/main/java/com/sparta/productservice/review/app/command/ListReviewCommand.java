package com.sparta.productservice.review.app.command;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

public record ListReviewCommand(
	UUID mainProductId,
	Pageable pageable
) {
}
