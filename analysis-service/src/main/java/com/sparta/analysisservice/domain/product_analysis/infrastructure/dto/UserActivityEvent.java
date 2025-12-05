package com.sparta.analysisservice.domain.product_analysis.infrastructure.dto;

import java.time.Instant;
import java.util.UUID;

public record UserActivityEvent(
	UUID sessionId,
	UUID productId,
	String eventType,
	Instant timestamp,
	String meta
) {

	public static UserActivityEvent of(UUID sessionId, UUID productId, String eventType, String meta) {
		return new UserActivityEvent(sessionId, productId, eventType, Instant.now(), meta);
	}
}

