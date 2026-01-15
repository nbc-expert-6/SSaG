package com.sparta.recommendservice.domain.product_vector.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostgresInitRunner implements CommandLineRunner {
	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... args) {
		jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector;");
		jdbcTemplate.execute(
			"CREATE TABLE IF NOT EXISTS p_product_vector (" +
				"product_id UUID PRIMARY KEY, " +
				"embedding vector(128) NOT NULL, " +
				"updated_at TIMESTAMP NOT NULL DEFAULT NOW()" +
				");"
		);
		jdbcTemplate.execute(
			"CREATE INDEX IF NOT EXISTS idx_product_vector_embedding " +
				"ON p_product_vector USING hnsw (embedding vector_cosine_ops);"
		);
	}
}

