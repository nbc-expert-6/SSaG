package com.sparta.analysisservice.domain.product_analysis.infrastructure.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

import com.sparta.analysisservice.domain.product_analysis.application.service.PythonRunner;

@Component
public class PythonRunnerImpl implements PythonRunner {

	@Override
	public void run() throws InterruptedException, IOException {

		// 1. resources -> temp 파일
		InputStream is = getClass().getClassLoader().getResourceAsStream("python/kmeans.py");

		if (is == null) {
			throw new FileNotFoundException("kmeans.py not found in resources");
		}

		File tempFile = File.createTempFile("kmeans", ".py");
		tempFile.deleteOnExit(); // 자동으로 임시 파일 삭제
		Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

		tempFile.setExecutable(true);

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
				System.out.println(line);
			}
		}

		// 5. 종료 코드 확인
		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Python script failed with exit code " + exitCode);
		}
	}
}
