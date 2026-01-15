package com.sparta.recommendservice.domain.product_vector.domain.event;

import java.util.List;
import java.util.UUID;

public record EmbeddingUpdatedEvent(List<UUID> productIds) {
}
