package com.sparta.productservice.common.kafka;

public final class KafkaConsumerConstants {

	private KafkaConsumerConstants() {}

	/**
	 * Kafka Consumer 최대 시도 횟수 (1 + retry)
	 */
	public static final int MAX_ATTEMPT_COUNT = 3;

	/**
	 * Kafka Consumer 최대 시도 횟수 (1 + retry)
	 */
	public static final String MAX_ATTEMPT_COUNT_STR = "3";

	/**
	 * 재시도 초기 지연 시간 (ms)
	 */
	public static final long RETRY_DELAY_MS = 1000L;

	/**
	 * 재시도 백오프 배수
	 */
	public static final double BACKOFF_MULTIPLIER = 2.0;

	/**
	 * 최대 재시도 지연 시간 (ms)
	 */
	public static final long MAX_RETRY_DELAY_MS = 10_000L;
}