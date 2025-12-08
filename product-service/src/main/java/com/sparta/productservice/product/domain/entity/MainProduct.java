package com.sparta.productservice.product.domain.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import com.sparta.productservice.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_main_product")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainProduct extends BaseEntity {
	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@Column(name = "category_medium_id", nullable = false, columnDefinition = "uuid")
	private UUID categoryMediumId;

	@Column(name = "name", nullable = false, length = 500)
	private String name;

	@Column(name = "lowest_price", nullable = false)
	private BigDecimal lowestPrice;

	@Column(name = "image_url", nullable = false, length = 2000)
	private String imageUrl;

	@Column(name = "brand")
	private String brand;

	@Column(name = "click_count", nullable = false)
	private Long clickCount;

	@Column(name = "review_count", nullable = false)
	private Long reviewCount;

	@Column(name = "review_rating_avg", nullable = false)
	private BigDecimal reviewRatingAvg;

	@OneToMany(mappedBy = "mainProduct", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Product> products = new ArrayList<>();

	@Builder
	public MainProduct(UUID categoryMediumId, String name, BigDecimal lowestPrice,
		String imageUrl, String brand) {
		this.categoryMediumId = categoryMediumId;
		this.name = name;
		this.lowestPrice = lowestPrice;
		this.imageUrl = imageUrl;
		this.brand = brand;
		this.clickCount = 0L;
		this.reviewCount = 0L;
		this.reviewRatingAvg = BigDecimal.ZERO;
	}

	public void addProduct(Product product) {
		this.products.add(product);
		product.updateMainProduct(this);
	}

	public Integer getProductCount() {
		return products.size();
	}
}
