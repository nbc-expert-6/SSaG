package com.sparta.analysisservice.domain.product_analysis.domain.event;

public enum EventType {

	PAGE_ENTER("page_enter"),
	PAGE_EXIT("page_exit"),
	OTHER("other");

	private final String type;

	EventType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static EventType from(String v) {
		if (v == null) {
			return OTHER;
		}

		v = v.toLowerCase();
		for (EventType e : values()) {
			if (e.type.equals(v)) {
				return e;
			}
		}

		return OTHER;
	}
}
