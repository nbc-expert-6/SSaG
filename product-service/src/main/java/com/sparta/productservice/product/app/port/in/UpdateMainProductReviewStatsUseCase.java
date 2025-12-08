package com.sparta.productservice.product.app.port.in;

import com.sparta.productservice.product.app.command.UpdateMainProductReviewStatsCommand;

public interface UpdateMainProductReviewStatsUseCase {
	void updateReviewStats(UpdateMainProductReviewStatsCommand command);
}
