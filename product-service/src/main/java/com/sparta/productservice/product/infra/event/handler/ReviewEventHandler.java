package com.sparta.productservice.product.infra.event.handler;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.productservice.product.app.command.UpdateMainProductReviewStatsCommand;
import com.sparta.productservice.product.app.port.in.UpdateMainProductReviewStatsUseCase;
import com.sparta.productservice.common.kafka.message.ReviewCreatedMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventHandler {
	private final UpdateMainProductReviewStatsUseCase updateMainProductReviewStatsUseCase;

	@Transactional
	public void handleReviewCreated(ReviewCreatedMessage event) {
		log.info("Handling review created message {}", event);

		List<BigDecimal> ratings = event.reviews().stream().map(ReviewCreatedMessage.ReviewCreated::rating).toList();

		UpdateMainProductReviewStatsCommand command =
			new UpdateMainProductReviewStatsCommand(event.mainProductId(), ratings);

		updateMainProductReviewStatsUseCase.updateReviewStats(command);
	}
}
