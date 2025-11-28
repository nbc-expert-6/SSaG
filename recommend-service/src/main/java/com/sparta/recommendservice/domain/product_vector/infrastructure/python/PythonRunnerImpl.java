package com.sparta.recommendservice.domain.product_vector.infrastructure.python;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;

import com.sparta.recommendservice.domain.product_vector.application.PythonRunner;

@Component
public class PythonRunnerImpl implements PythonRunner {

	@Override
	public void run() throws InterruptedException, IOException {
		// OS에 따라 Python 실행 명령어 결정
		String pythonCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";

		// ProcessBuilder를 사용해서 외부 Python 스크립트를 실행
		ProcessBuilder pb = new ProcessBuilder(
			pythonCommand,
			"src/main/resources/vector/vector.py"
		);
		pb.redirectErrorStream(true); // stderr도 stdout에 합치기
		Process process = pb.start();

		// 출력 읽기
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		StringBuilder output = new StringBuilder();

		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}

		// Python 스크립트 종료 코드 가져오기 (0이면 정상, 0이 아니면 오류)
		int exitCode = process.waitFor();

		if (exitCode != 0) {
			throw new RuntimeException("Python script failed with exit code " + exitCode);
		}
	}
}
