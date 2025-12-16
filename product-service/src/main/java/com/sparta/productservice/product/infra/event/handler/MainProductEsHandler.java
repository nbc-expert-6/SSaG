package com.sparta.productservice.product.infra.event.handler;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sparta.productservice.product.domain.event.MainProductEsSyncEvent;
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

	@Async
	@EventListener
	public void handleSyncEsMainProduct(MainProductEsSyncEvent event) {
		List<MainProductEsSyncEvent.MainProductDto> products = event.getProducts();

		log.info("🚀 Start ES batch sync: total={} products", products.size());

		int successCount = 0;
		int failCount = 0;
		try {
			List<MainProductDocument> docs = products.stream()
				.map(dto -> MainProductDocument.builder()
					.id(dto.getId().toString())
					.name(dto.getName())
					.brand(dto.getBrand())
					.categoryId(dto.getCategoryId().toString())
					.lowestPrice(dto.getLowestPrice())
					.productCount(dto.getProductCount())
					.reviewCount(dto.getReviewCount())
					.rating(dto.getRating())
					.clickCount(dto.getClickCount())
					.imageUrl(dto.getImageUrl())
					.createdAt(dto.getCreatedAt())
					.deleted(dto.isDeleted())
					.build())
				.toList();

			searchRepository.saveAll(docs);
			successCount += 500;
		} catch (Exception e) {
			log.error("❌ Failed to sync ES doc: id={}, name={}");
			failCount += 500;
		}

		log.info("✅ ES sync completed: success={}, failed={}", successCount, failCount);
	}
}
