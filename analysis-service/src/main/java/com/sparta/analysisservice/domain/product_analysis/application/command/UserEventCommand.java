package com.sparta.analysisservice.domain.product_analysis.application.command;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEventCommand {
	private UUID productId;
	private String eventType;
	private String meta;
}
