package com.sparta.productservice.product.infra.event.handler;

import java.io.IOException;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sparta.productservice.common.kafka.KafkaTopicType;
import com.sparta.productservice.product.domain.event.MainProductEsSyncEvent;
import com.sparta.productservice.product.domain.event.ProductCreatedEvent;
import com.sparta.productservice.product.domain.repository.MainProductSearchRepository;
import com.sparta.productservice.common.kafka.publisher.DLTPublisher;
import com.sparta.productservice.product.infra.search.document.MainProductDocument;
import com.sparta.productservice.product.infra.search.mapper.MainProductDocumentMapper;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainProductEsHandler {

	private final MainProductSearchRepository searchRepository;
	private final MainProductDocumentMapper mainProductDocumentMapper;
	private final DLTPublisher DLTPublisher;
	private static final int MAX_ATTEMPTS = 3;

	/**
	 * 상품 생성 시 ES 동기화
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Retryable(
		retryFor = {ElasticsearchException.class, IOException.class},
		maxAttempts = MAX_ATTEMPTS,
		backoff = @Backoff(delay = 1000, multiplier = 2.0)
	)
	public void handleProductCreated(ProductCreatedEvent event) {
		MainProductDocument document = mainProductDocumentMapper.toDocument(event);
		searchRepository.save(document);
		log.info("✅ ES indexed: {}", document.getId());
	}

	/**
	 * 재시도 실패 시 DLQ 전송
	 */
	@Recover
	public void recoverProductCreated(Exception e, ProductCreatedEvent event) {
		int actualRetryCount = MAX_ATTEMPTS - 1;

		log.error("❌ Failed to index after {} retries (total {} attempts), sending to DLQ: {}",
			actualRetryCount, MAX_ATTEMPTS, event.getMainProductId(), e);

		DLTPublisher.sendToDLT(
			KafkaTopicType.PRODUCT_ES_SYNC,
			event.getMainProductId().toString(),
			event,
			e,
			actualRetryCount
		);
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
			log.error("❌ Failed to sync batch ES doc");
			failCount += 500;

			DLTPublisher.sendToDLT(
				KafkaTopicType.PRODUCT_ES_SYNC_BATCH,
				event.toString(),
				event,
				e,
				0  // 배치는 재시도 없이 바로 DLT
			);
		}

		log.info("ES sync completed: success={}, failed={}", successCount, failCount);
	}
}
