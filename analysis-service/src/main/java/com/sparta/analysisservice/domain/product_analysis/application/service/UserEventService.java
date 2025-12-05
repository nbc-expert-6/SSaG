package com.sparta.analysisservice.domain.product_analysis.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sparta.analysisservice.domain.product_analysis.application.command.UserEventCommand;
import com.sparta.analysisservice.domain.product_analysis.infrastructure.messaging.KafkaPublisher;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserEventService {

	private final KafkaPublisher kafkaPublisher;

	public void trackEvent(HttpSession session, UserEventCommand command) {
		UUID sessionId = (UUID)session.getAttribute("SESSION_UUID");
		if (sessionId == null) {
			sessionId = UUID.randomUUID();
			session.setAttribute("SESSION_UUID", sessionId);
		}

		kafkaPublisher.publishUserEvent(
			sessionId,
			command.getProductId(),
			command.getEventType(),
			command.getMeta()
		);
	}

}
