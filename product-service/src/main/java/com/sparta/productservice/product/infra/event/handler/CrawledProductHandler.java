package com.sparta.productservice.product.infra.event.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.product.app.port.in.CreateProductUseCase;
import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledProductHandler {

	private final CreateProductUseCase createProductUseCase;

	/**
	 * 크롤링된 신규 상품 처리
	 */
	@Transactional
	public void handleCrawledProduct(CrawledProductMessage message) {
		log.info("Handling crawled product: {}", message.mainProductId());
		createProductUseCase.createProduct(message.toCommand());
	}
}
