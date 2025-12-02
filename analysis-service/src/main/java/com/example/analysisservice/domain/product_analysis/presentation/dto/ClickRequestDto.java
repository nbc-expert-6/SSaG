package com.example.analysisservice.domain.product_analysis.presentation.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClickRequestDto {

	private UUID productId;

}
