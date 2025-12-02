package com.sparta.productservice.review.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sparta.productservice.review.domain.repository.ReviewRepository;
import com.sparta.productservice.review.infra.repository.JpaReviewRepository;
import com.sparta.productservice.review.infra.repository.ReviewQueryRepository;
import com.sparta.productservice.review.infra.repository.ReviewRepositoryAdapter;

@Configuration
public class ReviewRepositoryConfig {

	@Bean
	public ReviewRepository reviewRepository(JpaReviewRepository jpaReviewRepository, ReviewQueryRepository reviewQueryRepository) {
		return new ReviewRepositoryAdapter(jpaReviewRepository, reviewQueryRepository);
	}
}
