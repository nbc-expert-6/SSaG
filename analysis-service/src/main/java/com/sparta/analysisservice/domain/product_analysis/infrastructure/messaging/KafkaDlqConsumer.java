package com.sparta.analysisservice.domain.product_analysis.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.analysisservice.domain.product_analysis.infrastructure.dto.UserActivityEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaDlqConsumer {

	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "user-event-dlq", groupId = "dlq-user-event-group")
	public void listenUserEventDlq(String payload) {
		UserActivityEvent event = deserialize(payload);
		if (event != null) {
			log.error("[DLQ 수신 - user-event.dlq] 비정상 처리된 이벤트 감지 -> {}", event);
		}
	}

	@KafkaListener(topics = "product-analysis-dlq", groupId = "dlq-product-analysis-group")
	public void listenProductAnalysisDlq(String payload) {
		UserActivityEvent event = deserialize(payload);
		if (event != null) {
			log.error("[DLQ 수신 - product-analysis-dlq] Python 분석 파이프라인 전달 실패 이벤트 감지 -> {}", event);
		}
	}

	private UserActivityEvent deserialize(String payload) {
		try {
			return objectMapper.readValue(payload, UserActivityEvent.class);
		} catch (Exception e) {
			log.error("DLQ 메시지 역직렬화 실패 → payload={}", payload, e);
			return null;
		}
	}

}
