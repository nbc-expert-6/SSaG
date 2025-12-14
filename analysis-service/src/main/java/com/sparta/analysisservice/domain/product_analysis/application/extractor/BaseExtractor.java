package com.sparta.analysisservice.domain.product_analysis.application.extractor;

import java.io.Serializable;
import java.util.UUID;

import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

public interface BaseExtractor {

	boolean supports(String eventType);

	Extracted extract(UserEventDocument event);

	@Getter
	@AllArgsConstructor
	@EqualsAndHashCode
	@ToString
	class Extracted implements Serializable {
		private final String eventType;
		private final UUID productId;
		private final UUID sessionId;
		private final Long timestamp;
		private final String metaJson;

	}

}
