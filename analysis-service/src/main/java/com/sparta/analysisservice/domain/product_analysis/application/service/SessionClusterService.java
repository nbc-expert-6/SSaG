package com.sparta.analysisservice.domain.product_analysis.application.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sparta.analysisservice.domain.product_analysis.presentation.dto.SessionClusterRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionClusterService {

	private final Map<UUID, SessionClusterRequest.SessionDto> sessionStore = new ConcurrentHashMap<>();

	private final RedisTemplate<String, String> redisTemplate;
	private static final Duration ANOMALY_TTL = Duration.ofHours(1); // 필요 시 TTL 설정
	private static final String ANOMALY_KEY_PREFIX = "anomalous:session:";

	public void processClusterData(SessionClusterRequest request) {
		for (SessionClusterRequest.SessionDto s : request.getSessions()) {
			UUID sessionId = UUID.fromString(s.getSessionId());
			sessionStore.put(sessionId, s);
		}
	}

	// 이상치 세션 마킹
	public void markAnomalous(String sessionId) {
		String key = ANOMALY_KEY_PREFIX + sessionId;
		redisTemplate.opsForValue().set(key, "ANOMALY", ANOMALY_TTL);
	}

	// 이상치 여부 조회
	public boolean isAnomalous(String sessionId) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(ANOMALY_KEY_PREFIX + sessionId));
	}

	public boolean isAnomalousSession(UUID sessionId) {
		SessionClusterRequest.SessionDto dto = sessionStore.get(sessionId);
		boolean result = dto != null && dto.isAnomalous();
		if (result) {
			System.out.println("[ANOMALY CHECK] sessionId=" + sessionId + " is anomalous");
		}
		return result;
	}

	// 클러스터별 총 세션 수
	public Map<Integer, Long> getClusterTotalCount() {
		return sessionStore.values().stream()
			.collect(Collectors.groupingBy(SessionClusterRequest.SessionDto::getCluster, Collectors.counting()));
	}

	// 클러스터별 이상치 세션 수
	public Map<Integer, Long> getClusterAnomalousCount() {
		return sessionStore.values().stream()
			.filter(SessionClusterRequest.SessionDto::isAnomalous)
			.collect(Collectors.groupingBy(SessionClusterRequest.SessionDto::getCluster, Collectors.counting()));
	}

	// 클러스터별 이상치 비율 계산
	public Map<Integer, Double> getClusterAnomalyRatio() {
		Map<Integer, Long> total = getClusterTotalCount();
		Map<Integer, Long> anomalous = getClusterAnomalousCount();

		Map<Integer, Double> ratioMap = new HashMap<>();
		total.forEach((clusterId, totalCount) -> {
			long anomalousCount = anomalous.getOrDefault(clusterId, 0L);
			double ratio = (double)anomalousCount / totalCount * 100.0;
			ratioMap.put(clusterId, ratio);
		});
		return ratioMap;
	}
}
