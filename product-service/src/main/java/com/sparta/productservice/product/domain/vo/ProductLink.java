package com.sparta.productservice.product.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLink {

	@Column(name = "sale_link", nullable = false, length = 2000)
	private String url;

	@Enumerated(EnumType.STRING)
	@Column(name = "platform_type", nullable = false)
	private PlatformType platformType;

	@Builder
	public ProductLink(String url, PlatformType platformType) {
		validateUrl(url);
		this.url = url;
		this.platformType = platformType;
	}

	private void validateUrl(String url) {
		if (url == null || url.isBlank()) {
			throw new IllegalArgumentException("URL은 필수입니다.");
		}
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			throw new IllegalArgumentException("유효한 URL 형식이 아닙니다.");
		}
	}
}
