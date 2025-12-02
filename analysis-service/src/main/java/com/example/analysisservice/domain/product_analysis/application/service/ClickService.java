package com.example.analysisservice.domain.product_analysis.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.analysisservice.domain.product_analysis.infrastructure.messaging.KafkaPublisher;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClickService {

	private final KafkaPublisher kafkaPublisher;

	public void trackClick(HttpSession session, UUID productId) {
		UUID sessionId = (UUID)session.getAttribute("SESSION_UUID");

		if (sessionId == null) {
			sessionId = UUID.randomUUID();
			session.setAttribute("SESSION_UUID", sessionId);
		}

		LocalDateTime clickedAt = LocalDateTime.now();

		kafkaPublisher.publishClickEvent(sessionId, productId, clickedAt);
	}

}
