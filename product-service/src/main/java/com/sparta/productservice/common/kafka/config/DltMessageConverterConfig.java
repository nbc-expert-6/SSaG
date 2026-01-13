package com.sparta.productservice.common.kafka.config;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DltMessageConverterConfig {

	private final ObjectMapper objectMapper = new ObjectMapper()
		.registerModule(new JavaTimeModule());

	@Bean
	public RecordMessageConverter dltMessageConverter() {
		return new RecordMessageConverter() {
			@Override
			public Message<?> toMessage(ConsumerRecord<?, ?> record,
				Acknowledgment acknowledgment,
				Consumer<?, ?> consumer,
				Type payloadType) {

				Map<String, Object> headerMap = convertHeadersToMap(record.headers());

				logDltInfo(headerMap, record.value());

				return MessageBuilder
					.withPayload(record.value())
					.copyHeaders(headerMap)
					.build();
			}

			@Override
			public ProducerRecord<?, ?> fromMessage(Message<?> message, String defaultTopic) {
				throw new UnsupportedOperationException("DLT consumer does not produce messages");
			}

			private Map<String, Object> convertHeadersToMap(Headers kafkaHeaders) {
				Map<String, Object> headerMap = new HashMap<>();

				kafkaHeaders.forEach(header -> {
					String key = header.key();
					byte[] value = header.value();

					if (value != null) {
						Object convertedValue = convertHeaderValue(key, value);
						headerMap.put(key, convertedValue);
					}
				});

				return headerMap;
			}

			private Object convertHeaderValue(String key, byte[] value) {
				if (key.equals("kafka_original-offset") ||
					key.equals("kafka_original-timestamp") ||
					key.equals("retry_topic-original-timestamp") ||
					key.equals("retry_topic-backoff-timestamp")) {
					return bytesToLong(value);
				}

				if (key.equals("kafka_original-partition") ||
					key.equals("retry_topic-attempts")) {
					return bytesToInt(value);
				}

				return new String(value, StandardCharsets.UTF_8);
			}

			private Long bytesToLong(byte[] bytes) {
				if (bytes == null || bytes.length != 8)
					return null;
				long value = 0;
				for (int i = 0; i < 8; i++) {
					value = (value << 8) | (bytes[i] & 0xFF);
				}
				return value;
			}

			private Integer bytesToInt(byte[] bytes) {
				if (bytes == null || bytes.length != 4)
					return null;
				int value = 0;
				for (int i = 0; i < 4; i++) {
					value = (value << 8) | (bytes[i] & 0xFF);
				}
				return value;
			}

			private void logDltInfo(Map<String, Object> headers, Object message) {
				String originalTopic = (String)headers.getOrDefault("original_topic",
					headers.getOrDefault("kafka_original-topic", "unknown"));

				String exceptionType = (String)headers.getOrDefault("exception_type",
					headers.getOrDefault("kafka_exception-cause-fqcn", "unknown"));

				String exceptionMessage = (String)headers.getOrDefault("exception_message",
					headers.getOrDefault("kafka_exception-message", "unknown"));

				Long originalOffset = (Long)headers.get("kafka_original-offset");
				Integer originalPartition = (Integer)headers.get("kafka_original-partition");
				Integer retryAttempts = (Integer)headers.get("retry_topic-attempts");
				Long originalTimestamp = (Long)headers.get("kafka_original-timestamp");

				String timestampStr = (String)headers.getOrDefault("timestamp_readable",
					originalTimestamp != null ? Instant.ofEpochMilli(originalTimestamp).toString() : "unknown");

				log.error("DLT - Topic: {}, Offset: {}, Partition: {}, Timestamp: {}",
					originalTopic, originalOffset, originalPartition, timestampStr);
				log.error("Exception: {}, Reason: {}, Attempts: {}",
					exceptionType, exceptionMessage, retryAttempts);

				try {
					String messageJson = objectMapper.writeValueAsString(message);
					log.error("Failed message: {}", messageJson);
				} catch (Exception e) {
					log.error("Failed message: {}", message);
				}
			}
		};
	}
}