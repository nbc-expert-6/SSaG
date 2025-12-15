package com.sparta.analysisservice.domain.product_analysis.application.extractor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventExtractor {

	// elasticsearch에 쿼리를 날리고 결과를 받아오는 객체
	private final ElasticsearchOperations elasticsearchOperations;

	public List<UserEventDocument> fetchClickEvents(Instant start, Instant end) {

		// 조건
		Criteria criteria = new Criteria("eventType").is("CLICK")
			.and("timestamp").between(start, end)
			.and("kafkaPublished").is(false);
		CriteriaQuery query = new CriteriaQuery(criteria);

		SearchHits<UserEventDocument> searchHits =
			elasticsearchOperations.search(query, UserEventDocument.class);

		List<UserEventDocument> results = searchHits.get()
			.map(hit -> hit.getContent())
			.collect(Collectors.toList());

		log.info("[ClickEventExtractor] {} CLICK events fetched", results.size());

		return results;
	}

}
