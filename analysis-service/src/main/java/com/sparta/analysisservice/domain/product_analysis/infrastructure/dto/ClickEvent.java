package com.sparta.analysisservice.domain.product_analysis.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClickEvent(
	UUID sessionId,
	UUID productId,
	LocalDateTime clickedAt
) {
}

