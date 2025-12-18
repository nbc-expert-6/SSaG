package com.sparta.productservice.common.kafka;

public final class KafkaConsumerConstants {
	private KafkaConsumerConstants() {
	}

	/**
	 * Kafka Consumer 최대 재시도 횟수
	 */
	public static final String MAX_ATTEMPT_COUNT = "3";

	/**
	 * 실제 재시도 횟수 (MAX_ATTEMPT_COUNT - 1)
	 */
	public static final int ACTUAL_RETRY_COUNT = 2;

	/**
	 * 재시도 초기 지연 시간 (ms)
	 */
	public static final long RETRY_DELAY_MS = 1000L;

	/**
	 * 재시도 백오프 배수
	 */
	public static final double BACKOFF_MULTIPLIER = 2.0;
}