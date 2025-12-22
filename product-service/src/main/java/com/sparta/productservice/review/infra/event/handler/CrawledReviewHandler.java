package com.sparta.productservice.review.infra.event.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.review.app.port.in.CreateReviewsUseCase;
import com.sparta.productservice.review.infra.event.message.CrawledReviewMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledReviewHandler {

	private final CreateReviewsUseCase createReviewsUseCase;

	/**
	 * 크롤링된 신규 상품의 리뷰 처리
	 */
	@Transactional
	public void handleCrawledReview(CrawledReviewMessage message) {
		log.info("Handling crawled review: {}", message.mainProductId());
		createReviewsUseCase.createReviews(message.toCommand());
	}
}
