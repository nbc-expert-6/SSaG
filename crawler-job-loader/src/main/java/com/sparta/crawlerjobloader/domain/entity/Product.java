package com.sparta.crawlerjobloader.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import com.sparta.batch.domain.vo.ProductLink;
import com.sparta.batch.domain.vo.ProductPrice;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_product")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "main_product_id", nullable = false, columnDefinition = "uuid")
	private MainProduct mainProduct;

	@Column(name = "name", nullable = false, length = 500)
	private String name;

	@Embedded
	private ProductPrice productPrice;

	@Embedded
	private ProductLink productLink;

}
