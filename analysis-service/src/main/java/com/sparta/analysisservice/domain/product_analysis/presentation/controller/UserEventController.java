package com.sparta.analysisservice.domain.product_analysis.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.analysisservice.domain.product_analysis.application.command.UserEventCommand;
import com.sparta.analysisservice.domain.product_analysis.application.service.EventEtlService;
import com.sparta.analysisservice.domain.product_analysis.application.service.UserEventService;
import com.sparta.analysisservice.domain.product_analysis.presentation.dto.UserEventRequestDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserEventController {

	private final UserEventService userEventService;
	private final EventEtlService eventEtlService;

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

}
