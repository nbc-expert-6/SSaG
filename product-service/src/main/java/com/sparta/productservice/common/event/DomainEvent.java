package com.sparta.productservice.common.event;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public abstract class DomainEvent {
	private final LocalDateTime occurredAt;

	protected DomainEvent() {
		this.occurredAt = LocalDateTime.now();
	}
}
