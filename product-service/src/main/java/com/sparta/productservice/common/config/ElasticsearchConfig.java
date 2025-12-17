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

		// HTTPS 사용시 SSL 활성화
		if (elasticsearchUri.startsWith("https://")) {
			builder.usingSsl();
		}

		if (username != null && !username.isEmpty()) {
			builder.withBasicAuth(username, password);
		}

		return builder
			.withConnectTimeout(parseTimeout(connectTimeout))
			.withSocketTimeout(parseTimeout(socketTimeout))
			.build();
	}

	private String parseUri(String uri) {
		String result = uri.replace("http://", "").replace("https://", "");

		// 포트가 없으면 기본 포트 추가
		if (!result.contains(":")) {
			if (uri.startsWith("https://")) {
				result = result + ":443";
			} else {
				result = result + ":9200";
			}
		}

		return result;
	}

	private long parseTimeout(String timeout) {
		// "5s" -> 5000, "30s" -> 30000
		if (timeout.endsWith("s")) {
			return Long.parseLong(timeout.substring(0, timeout.length() - 1)) * 1000;
		}
		return Long.parseLong(timeout);
	}
}
