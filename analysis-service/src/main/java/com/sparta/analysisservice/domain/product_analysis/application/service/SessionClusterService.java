package com.sparta.analysisservice.domain.product_analysis.application.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sparta.analysisservice.domain.product_analysis.presentation.dto.SessionClusterRequest;

@Service
public class SessionClusterService {

	private final Map<String, SessionClusterRequest.SessionDto> sessionStore = new ConcurrentHashMap<>();

	public void processClusterData(SessionClusterRequest request) {
		for (SessionClusterRequest.SessionDto s : request.getSessions()) {
			sessionStore.put(s.getSessionId(), s);
		}
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
