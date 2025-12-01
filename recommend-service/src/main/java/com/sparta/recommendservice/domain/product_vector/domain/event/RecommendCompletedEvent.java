package com.sparta.recommendservice.domain.product_vector.domain.event;

import java.util.List;
import java.util.UUID;

public record RecommendCompletedEvent(UUID productId, List<UUID> recommendedIds) {
}