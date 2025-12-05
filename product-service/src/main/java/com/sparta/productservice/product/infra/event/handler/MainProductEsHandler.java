package com.sparta.productservice.product.infra.event.handler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sparta.productservice.product.domain.event.ProductCreatedEvent;
import com.sparta.productservice.product.domain.repository.MainProductSearchRepository;
import com.sparta.productservice.product.infra.search.document.MainProductDocument;
import com.sparta.productservice.product.infra.search.mapper.MainProductDocumentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainProductEsHandler {

	private final MainProductSearchRepository searchRepository;
	private final MainProductDocumentMapper mainProductDocumentMapper;

	/**
	 * 상품 생성 시 ES 동기화
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleProductCreated(ProductCreatedEvent event) {
		try {
			MainProductDocument document = mainProductDocumentMapper.toDocument(event);
			searchRepository.save(document);
			log.info("✅ ES indexed: {}", document.getId());
		} catch (Exception e) {
			log.error("❌ Failed to index to ES: {}", event.getMainProductId(), e);
			throw e;
		}
	}
}
