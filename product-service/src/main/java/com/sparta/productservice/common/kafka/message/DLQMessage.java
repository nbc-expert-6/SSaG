package com.sparta.productservice.common.kafka.message;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record DLQMessage(
	UUID messageId,
	String topic,
	String key,
	JsonNode originalPayload,
	String errorMessage,
	String stackTrace,
	int retryCount,
	LocalDateTime failedAt,
	LocalDateTime lastRetryAt
) {
	public static DLQMessage of(
		String topic,
		String key,
		Object payload,
		Exception error,
		int retryCount
	) {
		return new DLQMessage(
			java.util.UUID.randomUUID(),
			topic,
			key,
			convertToJsonNode(payload),
			error.getMessage(),
			getStackTraceAsString(error),
			retryCount,
			LocalDateTime.now(),
			null
		);
	}

	public static DLQMessage of(
		String topic,
		String key,
		Object payload,
		String errorMessage,
		String stackTrace,
		int retryCount
	) {
		return new DLQMessage(
			java.util.UUID.randomUUID(),
			topic,
			key,
			convertToJsonNode(payload),
			errorMessage,
			stackTrace,
			retryCount,
			LocalDateTime.now(),
			null
		);
	}

	private static JsonNode convertToJsonNode(Object payload) {
		try {
			com.fasterxml.jackson.databind.ObjectMapper mapper =
				new com.fasterxml.jackson.databind.ObjectMapper();
			return mapper.valueToTree(payload);
		} catch (Exception e) {
			return null;
		}
	}

	private static String getStackTraceAsString(Exception error) {
		java.io.StringWriter sw = new java.io.StringWriter();
		error.printStackTrace(new java.io.PrintWriter(sw));
		return sw.toString();
	}
}