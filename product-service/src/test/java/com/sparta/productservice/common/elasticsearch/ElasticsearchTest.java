package com.sparta.productservice.common.elasticsearch;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ElasticsearchTest {

	@Autowired
	private ElasticsearchOperations operations;

	@BeforeEach
	void setUp() {
		IndexOperations indexOps = operations.indexOps(TestProductDocument.class);
		if (indexOps.exists()) {
			indexOps.delete();
		}
		indexOps.create();
	}

	@AfterEach
	void tearDown() {
		operations.indexOps(TestProductDocument.class).delete();
	}

	@Test
	@DisplayName("문서 저장 및 조회 테스트")
	void saveAndGet() {
		// given
		TestProductDocument document = TestProductDocument.builder()
			.id("1")
			.name("나이키 운동화")
			.price(100000)
			.build();

		// when
		operations.save(document);
		TestProductDocument result = operations.get("1", TestProductDocument.class);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("1");
		assertThat(result.getName()).isEqualTo("나이키 운동화");
		assertThat(result.getPrice()).isEqualTo(100000);

		System.out.println("✅ 저장/조회 성공!");
		System.out.println("ID: " + result.getId());
		System.out.println("이름: " + result.getName());
		System.out.println("가격: " + result.getPrice());
	}
}
