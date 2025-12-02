package com.sparta.analysisservice.domain.product_analysis.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.sparta.analysisservice.domain.product_analysis.domain.entity.ClickEventDocument;

public interface ClickRepository extends ElasticsearchRepository<ClickEventDocument, String> {

	List<ClickEventDocument> findBySessionId(UUID sessionId);

}
