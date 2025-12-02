package com.sparta.analysisservice.domain.product_analysis.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.elasticsearch.annotations.Document;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@Document(indexName = "click-events")
public class ClickEventDocument {

	@Id
	private String id;

	private UUID sessionId;

	private UUID productId;

	private LocalDateTime clickedAt;

	@Builder
	public ClickEventDocument(UUID sessionId, UUID productId, LocalDateTime clickedAt) {
		this.sessionId = sessionId;
		this.productId = productId;
		this.clickedAt = clickedAt;
	}

}
