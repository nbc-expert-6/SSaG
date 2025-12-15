package com.sparta.analysisservice.domain.product_analysis.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sparta.analysisservice.domain.product_analysis.application.service.SessionClusterService;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
public class ClusterMetricsConfig {

	@Bean
	public ClusterAnomalyMetric clusterAnomalyMetric(SessionClusterService clusterService, MeterRegistry registry) {
		return new ClusterAnomalyMetric(clusterService, registry);
	}

	@Slf4j
	static class ClusterAnomalyMetric {
		private final SessionClusterService clusterService;
		private final MeterRegistry registry;

		public ClusterAnomalyMetric(SessionClusterService clusterService, MeterRegistry registry) {
			this.clusterService = clusterService;
			this.registry = registry;
		}

		@PostConstruct
		public void init() {
			for (int clusterId = 0; clusterId < 10; clusterId++) {
				final int id = clusterId;
				Gauge.builder("cluster_anomaly_ratio",
						() -> clusterService.getClusterAnomalyRatio().getOrDefault(id, 0.0))
					.description("Cluster anomaly ratio (DBSCAN detected)")
					.tag("clusterId", String.valueOf(id))
					.register(registry);
				log.info("Registered Gauge for clusterId={}", id);
			}
		}

	}
}

