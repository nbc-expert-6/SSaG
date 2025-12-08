package com.sparta.productservice.review.app.port.in;

import com.sparta.productservice.review.app.command.CreateReviewsCommand;

public interface CreateReviewsUseCase {
	void createReviews(CreateReviewsCommand command);
}
