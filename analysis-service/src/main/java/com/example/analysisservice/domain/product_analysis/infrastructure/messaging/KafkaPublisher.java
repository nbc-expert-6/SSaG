package com.example.analysisservice.domain.product_analysis.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.analysisservice.domain.product_analysis.infrastructure.dto.ClickEvent;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@CircuitBreaker(name = "kafkaPublishCB", fallbackMethod = "fallbackPublishClickEvent")
	public void publishClickEvent(UUID sessionId, UUID productId, LocalDateTime clickedAt) {
		ClickEvent event = new ClickEvent(sessionId, productId, clickedAt);
		kafkaTemplate.send("click.event", event);
		log.info("Kafka 메시지 전송 성공: click.event -> sessionId={}, productId={}", sessionId, productId);
	}

	public void fallbackPublishClickEvent(UUID sessionId, UUID productId, LocalDateTime clickedAt, Throwable e) {
		log.error("Kafka click.event publish failed -> sessionId={}, productId={}, error={}",
			sessionId, productId, e.getMessage(), e);
		// 모니터링, dead-letter 큐 적재 등 필요 시 구현
	}

}
