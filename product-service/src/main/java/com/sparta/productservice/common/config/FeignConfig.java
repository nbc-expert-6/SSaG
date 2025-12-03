package com.sparta.productservice.common.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.QueryMapEncoder;
import feign.codec.EncodeException;
import feign.querymap.BeanQueryMapEncoder;

@Configuration
public class FeignConfig {

	/**
	 * Feign QueryMapEncoder 커스터마이징
	 *
	 * @SpringQueryMap 사용 시 null 값을 query parameter에서 제외합니다.
	 *
	 * 예시:
	 * PageRequest(page=0, size=10, sortBy=null, direction=null)
	 * 결과: ?page=0&size=10 (sortBy, direction 제외)
	 *
	 *
	 * @return null 값을 제외하는 QueryMapEncoder
	 * @author skfkgla
	 */
	@Bean
	public QueryMapEncoder queryMapEncoder() {
		return new QueryMapEncoder() {
			private final BeanQueryMapEncoder delegate = new BeanQueryMapEncoder();

			@Override
			public Map<String, Object> encode(Object object) throws EncodeException {
				Map<String, Object> map = delegate.encode(object);

				map.entrySet().removeIf(entry ->
					entry.getValue() == null
				);

				return map;
			}
		};
	}
}
