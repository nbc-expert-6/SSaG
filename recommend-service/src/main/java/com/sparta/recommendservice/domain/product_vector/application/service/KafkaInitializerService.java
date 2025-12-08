package com.sparta.recommendservice.domain.product_vector.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// Kafka 토픽 1000개 넣어 시간 측정 테스트용
// TODO : 배포할때는 지우기
@Service
@RequiredArgsConstructor
public class KafkaInitializerService {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void sendBulkMessages() {
		System.out.println("Kafka 메시지 전송 시작!");
		String topic = "product.analysis";
		String sessionId = UUID.randomUUID().toString();
		Instant baseTime = Instant.parse("2025-12-04T11:11:17.000Z");

		for (int i = 0; i < 10; i++) {
			String productId = UUID.randomUUID().toString();
			String timestamp = baseTime.plus(i, ChronoUnit.SECONDS).toString();

			String message = String.format(
				"{\"sessionId\":\"%s\",\"productId\":\"%s\",\"eventType\":\"CLICK\",\"timestamp\":\"%s\",\"meta\":null}",
				sessionId, productId, timestamp
			);

			kafkaTemplate.send(topic, message);

			if (i % 100 == 0) {
				System.out.println("Sent message count: " + i);
			}
		}

		System.out.println("총 1000개의 메시지 전송 완료!");
	}
}
