package com.sparta.productservice.common.kafka;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sparta.productservice.common.event.DomainEvent;
import com.sparta.productservice.common.kafka.message.ReviewCreatedMessage;
import com.sparta.productservice.product.domain.event.MainProductEsSyncEvent;
import com.sparta.productservice.product.domain.event.ProductCreatedEvent;
import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;
import com.sparta.productservice.review.infra.event.message.CrawledReviewMessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopicType {
	PRODUCT_DETAILS(Topics.PRODUCT_DETAILS) {
		@Override
		public Class<?> valueClass() {
			return CrawledProductMessage.class;
		}
	},

	PRODUCT_REVIEWS(Topics.PRODUCT_REVIEWS) {
		@Override
		public Class<?> valueClass() {
			return CrawledReviewMessage.class;
		}
	},

	PRODUCT_CREATED(Topics.PRODUCT_CREATED) {
		@Override
		public Class<?> valueClass() {
			return ProductCreatedEvent.class;
		}
	},

	REVIEW_CREATED(Topics.REVIEW_CREATED) {
		@Override
		public Class<?> valueClass() {
			return ReviewCreatedMessage.class;
		}
	},

	// =========================
	// ES Sync
	// =========================
	PRODUCT_ES_SYNC(Topics.PRODUCT_ES_SYNC) {
		@Override
		public Class<?> valueClass() {
			return MainProductEsSyncEvent.class;
		}
	},

	PRODUCT_ES_SYNC_BATCH(Topics.PRODUCT_ES_SYNC_BATCH) {
		@Override
		public Class<?> valueClass() {
			return DomainEvent.class;
		}
	};

	private final String topic;

	private static final Map<String, KafkaTopicType> TOPIC_MAP;

	static {
		TOPIC_MAP = Arrays.stream(values())
			.collect(Collectors.toMap(
				KafkaTopicType::getTopic,
				Function.identity()
			));
	}

	public abstract Class<?> valueClass();

	public String dlt() {
		return topic + Topics.DLT_TOPIC_SUFFIX;
	}

	public String retry(int attempt) {
		return topic + "-retry-" + attempt;
	}

	public static KafkaTopicType fromTopic(String topic) {
		KafkaTopicType type = TOPIC_MAP.get(topic);
		if (type == null) {
			throw new IllegalArgumentException("매핑되지 않은 Kafka topic: " + topic);
		}
		return type;
	}

	/**
	 * Kafka Topic 상수
	 * @KafkaListener의 topics 속성에서 직접 사용 가능
	 */
	public static class Topics {
		public static final String PRODUCT_DETAILS = "product-details";
		public static final String PRODUCT_REVIEWS = "product-reviews";
		public static final String PRODUCT_CREATED = "product-created";
		public static final String REVIEW_CREATED = "review-created-events";
		public static final String PRODUCT_ES_SYNC = "product-es-sync";
		public static final String PRODUCT_ES_SYNC_BATCH = "product-es-sync-batch";

		public static final String DLT_TOPIC_SUFFIX = "-dlq";
	}
}
