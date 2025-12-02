package com.sparta.productservice.review.infra.repository;

import static com.sparta.productservice.review.domain.entity.QReview.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.sparta.productservice.common.querydsl.QuerydslRepositorySupport;
import com.sparta.productservice.review.domain.entity.Review;
import com.sparta.productservice.review.domain.repository.dto.RatingCountQuery;

@Repository
public class ReviewQueryRepository extends QuerydslRepositorySupport {
	public ReviewQueryRepository() {
		super(Review.class);
	}

	public List<RatingCountQuery> getRatingDistributionByMainProductId(UUID mainProductId) {
		return select(
			Projections.constructor(
				RatingCountQuery.class,
				review.rating.value.floor().intValue(),
				review.count()
			)
		)
			.from(review)
			.where(
				review.mainProductId.eq(mainProductId)
			)
			.groupBy(review.rating.value.floor())
			.orderBy(review.rating.value.floor().intValue().desc())
			.fetch();
	}
}
