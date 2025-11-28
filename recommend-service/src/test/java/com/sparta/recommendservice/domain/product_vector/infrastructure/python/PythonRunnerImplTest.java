package com.sparta.recommendservice.domain.product_vector.infrastructure.python;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PythonRunnerImplTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private PrintStream originalOut;

	@BeforeEach
	void setUp() {
		originalOut = System.out;
		System.setOut(new PrintStream(outContent));
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	@Test
	@DisplayName("Python PostgreSQL 연결 테스트를 Java에서 실행하고 출력 확인")
	void testRunPostgresPythonScript() throws IOException, InterruptedException {
		PythonRunnerImpl runner = new PythonRunnerImpl();
		runner.run();

		String capturedOutput = outContent.toString();
		System.out.println("Captured Output:\n" + capturedOutput);

		assertTrue(capturedOutput.contains("PostgreSQL connection successful!"));
		assertTrue(capturedOutput.contains("Test query result:"));

	}

}