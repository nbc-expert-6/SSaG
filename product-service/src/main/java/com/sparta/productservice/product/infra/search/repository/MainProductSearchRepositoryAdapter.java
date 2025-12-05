package com.sparta.productservice.product.infra.search.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import com.sparta.productservice.product.app.command.SearchMainProductCommand;
import com.sparta.productservice.product.domain.repository.MainProductSearchRepository;
import com.sparta.productservice.product.domain.repository.dto.MainProductSearchResult;
import com.sparta.productservice.product.infra.search.document.MainProductDocument;
import com.sparta.productservice.product.infra.search.mapper.MainProductDocumentMapper;
import com.sparta.productservice.product.infra.search.mapper.MainProductSortMapper;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MainProductSearchRepositoryAdapter implements MainProductSearchRepository {

	private final EsMainProductSearchRepository esMainProductSearchRepository;
	private final ElasticsearchOperations elasticsearchOperations;
	private final MainProductSortMapper mainProductSortMapper;
	private final MainProductDocumentMapper documentMapper;

	@Override
	public void save(MainProductDocument document) {
		esMainProductSearchRepository.save(document);
	}

	@Override
	public Page<MainProductSearchResult> search(SearchMainProductCommand command) {
		BoolQuery boolQuery = buildSearchQuery(command);

		Sort sort = mainProductSortMapper.toEsSort(command.pageSort());

		Pageable pageable = PageRequest.of(
			command.pageNumber(),
			command.pageSize().getValue(),
			sort
		);

		NativeQuery query = NativeQuery.builder()
			.withQuery(q -> q.bool(boolQuery))
			.withPageable(pageable)
			.build();

		SearchHits<MainProductDocument> hits =
			elasticsearchOperations.search(query, MainProductDocument.class);

		List<MainProductSearchResult> content = hits.stream()
			.map(SearchHit::getContent)
			.map(documentMapper::toSearchResult)
			.toList();

		return new PageImpl<>(content, pageable, hits.getTotalHits());
	}

	/**
	 * 검색 조건을 BoolQuery로 구성
	 */
	private BoolQuery buildSearchQuery(SearchMainProductCommand command) {
		BoolQuery.Builder boolQuery = new BoolQuery.Builder();

		// ID IN 검색
		addProductIdsFilter(boolQuery, command.productIds());

		// 상품명 검색 (nori 기반)
		addProductNameFilter(boolQuery, command.productName());

		// 카테고리 필터
		addCategoryIdsFilter(boolQuery, command.categoryIds());

		// 삭제 안 된 것만
		addDeletedFilter(boolQuery);

		return boolQuery.build();
	}

	/**
	 * 상품 ID 필터 추가
	 */
	private void addProductIdsFilter(BoolQuery.Builder boolQuery, List<UUID> productIds) {
		if (productIds != null && !productIds.isEmpty()) {
			boolQuery.must(q -> q.terms(t -> t
				.field("id")
				.terms(ts -> ts.value(
					productIds.stream()
						.map(UUID::toString)
						.map(FieldValue::of)
						.toList()
				))
			));
		}
	}

	/**
	 * 상품명 필터 추가
	 */
	private void addProductNameFilter(BoolQuery.Builder boolQuery, String productName) {
		if (productName != null && !productName.isBlank()) {
			boolQuery.must(q -> q.match(m -> m
				.field("name")
				.query(productName)
			));
		}
	}

	/**
	 * 카테고리 ID 필터 추가
	 */
	private void addCategoryIdsFilter(BoolQuery.Builder boolQuery, List<UUID> categoryIds) {
		if (categoryIds != null && !categoryIds.isEmpty()) {
			boolQuery.filter(q -> q.terms(t -> t
				.field("categoryId")
				.terms(ts -> ts.value(
					categoryIds.stream()
						.map(UUID::toString)
						.map(FieldValue::of)
						.toList()
				))
			));
		}
	}

	/**
	 * 삭제되지 않은 항목만 필터링
	 */
	private void addDeletedFilter(BoolQuery.Builder boolQuery) {
		boolQuery.filter(q -> q.term(t -> t
			.field("deleted")
			.value(false)
		));
	}
}