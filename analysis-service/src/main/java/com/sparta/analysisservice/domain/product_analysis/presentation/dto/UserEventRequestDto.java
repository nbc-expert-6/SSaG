package com.sparta.analysisservice.domain.product_analysis.presentation.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEventRequestDto {

	private UUID productId;
	private String eventType; // CLICK, PAGE_VIEW, SCROLL
	private String meta; // URL, session info, browser data etc
}

