package com.sparta.productservice.product.infra.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.productservice.product.domain.entity.MainProduct;

public interface JpaMainProductRepository extends JpaRepository<MainProduct, UUID> {
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		    UPDATE MainProduct m
		    SET m.reviewCount = m.reviewCount + :count,
		        m.reviewRatingAvg = 
		            (m.reviewRatingAvg * m.reviewCount + :totalRating) 
		            / CAST((m.reviewCount + :count) AS bigdecimal)
		    WHERE m.id = :id
		""")
	void increaseReviewStatBatch(
		@Param("id") UUID id,
		@Param("count") long count,
		@Param("totalRating") BigDecimal totalRating
	);

	@Modifying
	@Query("""
		    UPDATE MainProduct m
		    SET m.clickCount = m.clickCount + 1
		    WHERE m.id = :id
		""")
	void increaseClick(@Param("id") UUID id);

	@Query("""
			select mp
			from MainProduct mp
			where exists (
				select 1
				from Product p
				where p.mainProduct = mp
			)
		""")
	Page<MainProduct> findAllWithProduct(Pageable pageable);
}
