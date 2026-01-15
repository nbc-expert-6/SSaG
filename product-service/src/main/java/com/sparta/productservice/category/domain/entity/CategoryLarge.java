package com.sparta.productservice.category.domain.entity;

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
@Table(name = "p_category_large")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryLarge extends BaseEntity {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@Column(name = "name", nullable = false)
	private String name;

	@OneToMany(mappedBy = "categoryLarge", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CategoryMedium> categoryMediums = new ArrayList<>();

	@Builder
	public CategoryLarge(String name) {
		this.name = name;
	}
}
