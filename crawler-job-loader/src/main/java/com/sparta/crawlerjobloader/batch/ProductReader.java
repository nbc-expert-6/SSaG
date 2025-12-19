package com.sparta.crawlerjobloader.batch;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

import com.sparta.batch.domain.entity.MainProduct;

import jakarta.persistence.EntityManagerFactory;

@Component
public class ProductReader {

	private final EntityManagerFactory emf;

	public ProductReader(EntityManagerFactory emf) {
		this.emf = emf;
	}

	@StepScope
	public ItemReader<MainProduct> productReader() {
		return new JpaPagingItemReaderBuilder<MainProduct>()
			.name("productReader")
			.entityManagerFactory(emf) // 여기서 DB 연결
			.queryString("SELECT p FROM MainProduct p ORDER BY p.createdAt ASC")
			.pageSize(100)
			.saveState(true)
			.build();
	}
}