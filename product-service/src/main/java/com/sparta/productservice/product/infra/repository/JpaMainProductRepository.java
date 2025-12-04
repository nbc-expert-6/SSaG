package com.sparta.productservice.product.infra.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.productservice.product.domain.entity.MainProduct;

public interface JpaMainProductRepository extends JpaRepository<MainProduct, UUID> {
	@Modifying
	@Query("""
		    UPDATE MainProduct m
		    SET m.reviewCount = m.reviewCount + 1,
		        m.reviewRatingAvg =
		            (m.reviewRatingAvg * m.reviewCount + :rating)
		            / (m.reviewCount + 1)
		    WHERE m.id = :id
		""")
	void increaseReviewStat(@Param("id") UUID id, @Param("rating") BigDecimal rating);

	@Modifying
	@Query("""
        UPDATE MainProduct m
        SET m.clickCount = m.clickCount + 1
        WHERE m.id = :id
    """)
	void increaseClick(@Param("id") UUID id);
}
