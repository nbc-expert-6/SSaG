package com.sparta.productservice.common.kafka.message;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;


public record DLTMessage(
	String originalTopic,
	String key,
	Object payload,
	String exceptionClass,
	String exceptionMessage,
	String stackTrace,
	int retryCount,
	LocalDateTime failedAt
) {

	public static DLTMessage of(
		String originalTopic,
		String key,
		Object payload,
		Throwable exception,
		int retryCount
	) {
		return new DLTMessage(
			originalTopic,
			key,
			payload,
			exception.getClass().getName(),
			exception.getMessage(),
			toStackTrace(exception),
			retryCount,
			LocalDateTime.now()
		);
	}

	private static String toStackTrace(Throwable exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}