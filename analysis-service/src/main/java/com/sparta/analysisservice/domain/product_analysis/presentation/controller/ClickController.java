package com.sparta.analysisservice.domain.product_analysis.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.analysisservice.domain.product_analysis.application.service.ClickService;
import com.sparta.analysisservice.domain.product_analysis.presentation.dto.ClickRequestDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ClickController {

	private final ClickService clickService;

	@PostMapping("/track-click")
	public ResponseEntity<Void> trackClick(@RequestBody ClickRequestDto req, HttpSession session) {
		clickService.trackClick(session, req.getProductId());
		return ResponseEntity.ok().build();
	}

}
