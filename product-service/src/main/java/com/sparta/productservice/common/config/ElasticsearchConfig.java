package com.sparta.productservice.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

	@Value("${spring.elasticsearch.uris}")
	private String elasticsearchUri;

	@Value("${spring.elasticsearch.connection-timeout}")
	private String connectTimeout;

	@Value("${spring.elasticsearch.socket-timeout}")
	private String socketTimeout;

	@Value("${spring.elasticsearch.username:#{null}}")
	private String username;

	@Value("${spring.elasticsearch.password:#{null}}")
	private String password;

	@Override
	public ClientConfiguration clientConfiguration() {
		ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
			.connectedTo(parseUri(elasticsearchUri));

		// 인증 정보가 있으면 추가
		if (username != null && !username.isEmpty()) {
			builder.withBasicAuth(username, password);
		}

		return builder
			.withConnectTimeout(parseTimeout(connectTimeout))
			.withSocketTimeout(parseTimeout(socketTimeout))
			.build();
	}

	private String parseUri(String uri) {
		return uri.replace("http://", "").replace("https://", "");
	}

	private long parseTimeout(String timeout) {
		// "5s" -> 5000, "30s" -> 30000
		if (timeout.endsWith("s")) {
			return Long.parseLong(timeout.substring(0, timeout.length() - 1)) * 1000;
		}
		return Long.parseLong(timeout);
	}
}
