package com.sparta.analysisservice.domain.product_analysis.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;

public interface UserEventRepository extends ElasticsearchRepository<UserEventDocument, String> {

	List<UserEventDocument> findBySessionId(UUID sessionId);

	List<UserEventDocument> findByEventType(String eventType);

}
