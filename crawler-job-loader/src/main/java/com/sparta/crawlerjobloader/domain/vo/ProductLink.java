package com.sparta.crawlerjobloader.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
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

}
