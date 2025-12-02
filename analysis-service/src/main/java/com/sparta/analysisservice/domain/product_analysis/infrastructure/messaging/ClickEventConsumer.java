package com.sparta.analysisservice.domain.product_analysis.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.sparta.analysisservice.domain.product_analysis.domain.entity.ClickEventDocument;
import com.sparta.analysisservice.domain.product_analysis.domain.repository.ClickRepository;
import com.sparta.analysisservice.domain.product_analysis.infrastructure.dto.ClickEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {

	private final ClickRepository repository;
	private final KafkaPublisher kafkaPublisher;

	@KafkaListener(topics = "click.event", groupId = "click-group")
	public void consume(ClickEvent event) {
		log.info("Kafka 메시지 수신: sessionId={}, productId={}", event.sessionId(), event.productId());

		//Elasticsearch 저장
		ClickEventDocument doc = ClickEventDocument.builder()
			.sessionId(event.sessionId())
			.productId(event.productId())
			.clickedAt(event.clickedAt())
			.build();

		repository.save(doc);
		log.info("Elasticsearch 저장 완료, id={}", doc.getId());

		//Kafka 이벤트 발행

	}

}
