package com.sparta.analysisservice.domain.product_analysis.presentation.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.analysisservice.domain.product_analysis.application.command.UserEventCommand;
import com.sparta.analysisservice.domain.product_analysis.application.service.EventEtlService;
import com.sparta.analysisservice.domain.product_analysis.application.service.PythonRunner;
import com.sparta.analysisservice.domain.product_analysis.application.service.SessionClusterService;
import com.sparta.analysisservice.domain.product_analysis.application.service.UserEventService;
import com.sparta.analysisservice.domain.product_analysis.presentation.dto.SessionClusterRequest;
import com.sparta.analysisservice.domain.product_analysis.presentation.dto.UserEventRequestDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserEventController {

	private final UserEventService userEventService;
	private final EventEtlService eventEtlService;
	private final PythonRunner pythonRunner;
	private final SessionClusterService clusterService;

	@PostMapping("/user-event")
	public ResponseEntity<Void> trackEvent(@RequestBody UserEventRequestDto req, HttpSession session) {
		UserEventCommand command = UserEventRequestDto.toCommand(req);
		userEventService.trackEvent(session, command);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/etl/run")
	public String runEtl() {
		new Thread(() -> eventEtlService.runEtlJob()).start();
		return "ETL Success!!";
	}

	@GetMapping("/etl/kmeans")
	public String runKmeans() throws IOException, InterruptedException {
		pythonRunner.run();
		return "Kmeans calculate Sucess!!";
	}

	@PostMapping("/session-cluster")
	public ResponseEntity<Void> receiveClusterResult(@RequestBody SessionClusterRequest request) {
		// 로그
		System.out.println("[SESSION-CLUSTER RECEIVED] sessions: " + request.getSessions().size() +
			", clusters: " + request.getClusterProfiles().size());

		clusterService.processClusterData(request);

		System.out.println("[SESSION-CLUSTER PROCESSED] successfully");

		return ResponseEntity.ok().build();
	}

}
