package com.sparta.productservice.product.infra.event;

import static org.awaitility.Awaitility.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sparta.productservice.product.infra.event.handler.CrawledProductHandler;
import com.sparta.productservice.product.infra.event.message.CrawledProductMessage;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
	partitions = 1,
	topics = {"product-details"},
	brokerProperties = {
		"listeners=PLAINTEXT://localhost:9092",
		"port=9092"
	}
)
@TestPropertySource(properties = {
	"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
	"spring.kafka.consumer.group-id=product",
	"spring.kafka.consumer.auto-offset-reset=earliest"
})
class CrawledProductConsumerTest {

	@MockitoBean
	private CrawledProductHandler handler;

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Test
	void consume_success_handle() {
		// given
		CrawledProductMessage message = createTestMessage();

		// when
		kafkaTemplate.send("product-details", message);

		// then
		await()
			.pollInterval(Duration.ofSeconds(1))
			.atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() -> {
				verify(handler, times(1)).handleCrawledProduct(any(CrawledProductMessage.class));
			});
	}

	private CrawledProductMessage createTestMessage() {
		return new CrawledProductMessage(
			"550e8400-e29b-41d4-a716-446655440000",
			"COUPANG",
			"https://example.com",
			"나이키",
			"나이키 드라이핏 티셔츠",
			"판매자",
			new BigDecimal("29000"),
			new BigDecimal("3000"),
			"https://example.com/image.jpg"
		);
	}
}