package com.sparta.productservice.review.app;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.common.kafka.message.ReviewCreatedMessage;
import com.sparta.productservice.review.app.command.CreateReviewsCommand;
import com.sparta.productservice.review.app.command.ListReviewCommand;
import com.sparta.productservice.review.app.dto.ListReviewResult;
import com.sparta.productservice.review.app.port.in.CreateReviewsUseCase;
import com.sparta.productservice.review.domain.entity.Review;
import com.sparta.productservice.review.domain.repository.ReviewRepository;
import com.sparta.productservice.review.domain.repository.dto.RatingCountQuery;
import com.sparta.productservice.review.infra.event.producer.ReviewEventProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService implements CreateReviewsUseCase {
	private final ReviewRepository reviewRepository;
	private final ReviewEventProducer reviewEventProducer;

	public ListReviewResult getReviewsByMainProductId(ListReviewCommand command) {
		Page<Review> reviews = reviewRepository.getReviewsByMainProductId(command.mainProductId(), command.pageable());

		Integer totalImageCount = reviewRepository.countImagesByMainProductId(command.mainProductId());

		List<RatingCountQuery> reviewRatingCount = reviewRepository.getReviewRatingCountByMainProductId(
			command.mainProductId());

		return ListReviewResult.from(reviews, totalImageCount, reviewRatingCount);
	}

	@Override
	@Transactional
	public void createReviews(CreateReviewsCommand command) {
		List<Review> reviews = reviewRepository.saveAll(command.toReviews());

		reviewEventProducer.publishReviewCreatedEvents(ReviewCreatedMessage.from(command.mainProductId(), command.platformType(), reviews));
	}
}
