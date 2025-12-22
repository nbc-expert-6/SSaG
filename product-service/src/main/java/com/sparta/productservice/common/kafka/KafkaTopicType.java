package com.sparta.productservice.common.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopicType {
	//main product
	PRODUCT_DETAILS(Topics.PRODUCT_DETAILS),
	PRODUCT_REVIEWS(Topics.PRODUCT_REVIEWS),
	PRODUCT_CREATED(Topics.PRODUCT_CREATED),
	PRODUCT_REVIEW(Topics.PRODUCT_REVIEW),
	REVIEW_CREATED(Topics.REVIEW_CREATED),

	//ES sync
	PRODUCT_ES_SYNC(Topics.PRODUCT_ES_SYNC),
	PRODUCT_ES_SYNC_BATCH(Topics.PRODUCT_ES_SYNC_BATCH);

	private final String topic;

	public String dlt() {
		return topic + Topics.DLT_TOPIC_SUFFIX;
	}

	public String retry(int attempt) {
		return topic + "-retry-" + attempt;
	}

	/**
	 * Kafka Topic 상수
	 * @KafkaListener의 topics 속성에서 직접 사용 가능
	 */
	public static class Topics {
		public static final String PRODUCT_DETAILS = "product-details";
		public static final String PRODUCT_REVIEWS = "product-reviews";
		public static final String PRODUCT_CREATED = "product-created";
		public static final String PRODUCT_REVIEW = "product-review";
		public static final String REVIEW_CREATED = "review-created-events";
		public static final String PRODUCT_ES_SYNC = "product-es-sync";
		public static final String PRODUCT_ES_SYNC_BATCH = "product-es-sync-batch";

		public static final String DLT_TOPIC_SUFFIX = "-dlq";
	}
}
