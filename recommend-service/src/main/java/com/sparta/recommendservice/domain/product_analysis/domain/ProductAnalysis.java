package com.sparta.recommendservice.domain.product_analysis.domain;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_product_analysis", schema = "recommend_service_db")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductAnalysis {

	@Id
	private UUID sessionId;

	@Column(name = "click_sequence", columnDefinition = "uuid[]", nullable = false)
	private List<UUID> clickSequence;

	public UUID getSessionId() {
		return this.sessionId;
	}

	public List<UUID> getClickSequence() {
		return Collections.unmodifiableList(this.clickSequence);
	}

}
