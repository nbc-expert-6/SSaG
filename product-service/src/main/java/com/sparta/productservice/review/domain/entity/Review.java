package com.sparta.productservice.review.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import com.sparta.productservice.common.entity.BaseEntity;
import com.sparta.productservice.product.domain.vo.PlatformType;
import com.sparta.productservice.review.domain.vo.ReviewRating;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_review")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@Embedded
	private ReviewRating rating;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "main_product_id", nullable = false, columnDefinition = "uuid")
	private UUID mainProductId;

	@Enumerated(EnumType.STRING)
	@Column(name = "platform_type", nullable = false)
	private PlatformType platformType;

	@Column(name = "title", nullable = false, length = 500)
	private String title;

	@OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ReviewImage> reviewImages = new ArrayList<>();

	@Builder
	public Review(ReviewRating rating, String content, UUID mainProductId,
		PlatformType platformType, String title) {
		this.rating = rating;
		this.content = content;
		this.mainProductId = mainProductId;
		this.platformType = platformType;
		this.title = title;
	}
}
