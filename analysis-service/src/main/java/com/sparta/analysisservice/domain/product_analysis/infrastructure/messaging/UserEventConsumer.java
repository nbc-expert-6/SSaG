package com.sparta.analysisservice.domain.product_analysis.infrastructure.messaging;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.sparta.analysisservice.domain.product_analysis.application.service.ClickEventExtractor;
import com.sparta.analysisservice.domain.product_analysis.domain.entity.UserEventDocument;
import com.sparta.analysisservice.domain.product_analysis.domain.repository.UserEventRepository;
import com.sparta.analysisservice.domain.product_analysis.infrastructure.dto.UserActivityEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

	private final UserEventRepository repository;
	private final KafkaPublisher kafkaPublisher;
	private final RedisTemplate<String, String> redisTemplate;
	private final ClickEventExtractor clickEventExtractor;

	private static final String SESSION_KEY_PREFIX = "user:session:";
	private static final Duration SESSION_TTL = Duration.ofMinutes(10);

	@KafkaListener(topics = "user.event", groupId = "user-group")
	public void consume(UserActivityEvent event) {
		logEventReceived(event);

		if (!isSessionActive(event.sessionId())) {
			startNewSession(event.sessionId());
		} else {
			refreshSessionTTL(event.sessionId());
		}

		saveEventToElasticsearch(event);
		publishClickEvents();
	}

	// 로그
	private void logEventReceived(UserActivityEvent event) {
		log.info("[CONSUME user.event] sessionId={}, eventType={}, productId={}, meta={}",
			event.sessionId(),
			event.eventType(),
			event.productId() != null ? event.productId() : "NONE",
			event.meta()
		);
	}

	// 세션 체크
	private boolean isSessionActive(UUID sessionId) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_KEY_PREFIX + sessionId));
	}

	private void startNewSession(UUID sessionId) {
		redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, "ACTIVE", SESSION_TTL);
		log.info("New session started ({} min tracking) {}", SESSION_TTL.toMinutes(), sessionId);
	}

	private void refreshSessionTTL(UUID sessionId) {
		redisTemplate.expire(SESSION_KEY_PREFIX + sessionId, SESSION_TTL);
		log.debug("Session TTL refreshed for {}", sessionId);
	}

	// ES 저장
	private void saveEventToElasticsearch(UserActivityEvent event) {
		UserEventDocument doc = UserEventDocument.builder()
			.sessionId(event.sessionId())
			.productId(event.productId())
			.eventType(event.eventType())
			.timestamp(event.timestamp())
			.meta(event.meta())
			.kafkaPublished(false)
			.build();

		repository.save(doc);
		log.info("Elasticsearch 저장 완료, id={}", doc.getId());
	}

	// ETL: 클릭 이벤트만 추출 후 Kafka 발행
	private void publishClickEvents() {
		ZoneId zone = ZoneId.systemDefault();

		Instant startOfDay = LocalDate.now()
			.atStartOfDay(zone)
			.toInstant();

		Instant endOfDay = LocalDate.now()
			.atTime(LocalTime.MAX)
			.atZone(zone)
			.toInstant();

		List<UserEventDocument> clickEvents =
			clickEventExtractor.fetchClickEvents(startOfDay, endOfDay);
		clickEvents.forEach(e -> {
			try {
				kafkaPublisher.publishProductAnalysisEvent(e.getSessionId(), e.getProductId(), e.getTimestamp());
				e.setKafkaPublished(true);
				repository.save(e);
				log.info("[ETL] 클릭 이벤트 발행 성공 sessionId={}, productId={}", e.getSessionId(), e.getProductId());

			} catch (Exception ex) {
				log.error("[ETL] 클릭 이벤트 발행 실패 sessionId={}, productId={}", e.getSessionId(), e.getProductId(), ex);
			}
		});
	}
}

