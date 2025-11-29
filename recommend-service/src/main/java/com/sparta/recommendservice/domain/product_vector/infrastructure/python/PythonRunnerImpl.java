package com.sparta.recommendservice.domain.product_vector.infrastructure.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

import com.sparta.recommendservice.domain.product_vector.application.service.PythonRunner;

@Component
public class PythonRunnerImpl implements PythonRunner {

	@Override
	public void run() throws InterruptedException, IOException {

		// 1. resources -> temp 파일
		InputStream is = getClass().getClassLoader().getResourceAsStream("vector/vector.py");

		if (is == null) {
			throw new FileNotFoundException("vector.py not found in resources");
		}

		// resource 안 파일은 JAR 안에 들어가면 실제 파일 경로가 없어 직접 실행 불가
		// 임시 파일을 복사해서 실제로 실행 가능한 경로로 만들어야함
		File tempFile = File.createTempFile("vector", ".py");
		tempFile.deleteOnExit(); // 자동으로 임시 파일 삭제
		Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

		// 2. python 명령어 선택
		String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";

		// 3. ProcessBuilder 실행
		ProcessBuilder pb = new ProcessBuilder(pythonCmd, tempFile.getAbsolutePath());
		pb.redirectErrorStream(true);
		Process process = pb.start();

		// 4. 출력 읽기 (에러 확인용)
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line); // 콘솔에 출력
			}
		}

		// 5. 종료 코드 확인
		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Python script failed with exit code " + exitCode);
		}
	}
}
