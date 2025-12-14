package com.sparta.analysisservice.domain.product_analysis.presentation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionClusterRequest {

	private List<SessionDto> sessions;
	private List<ClusterProfileDto> clusterProfiles;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SessionDto {
		private String sessionId;
		private int cluster;
		@JsonProperty("is_anomalous")
		private boolean isAnomalous;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ClusterProfileDto {
		private int cluster;
		private double enter_count;
		private double exit_count;
		private double total_duration;
		private double avg_duration;
		private double max_duration;
		private double min_duration;
	}
}
