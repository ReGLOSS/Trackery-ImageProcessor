package com.bokkurin.trackery.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * packageName    : com.bokkurin.trackery.service
 * fileName       : ImageProcessServiceTest
 * author         : durururuk
 * date           : 25. 6. 26.
 * description    : ImageProcessService 테스트코드
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 26.		durururuk		최초 생성
 */
class ImageProcessServiceTest {

	@Test
	void testConvertToWebP() throws IOException {
		InputStream imageStream = getClass().getResourceAsStream("/sample-image/image.jpg");
		assertNotNull(imageStream);
		byte[] imageBytes = imageStream.readAllBytes();

		ImageProcessService imageProcessService = new ImageProcessService();
		byte[] webpBytes = imageProcessService.convertToWebP(imageBytes);

		assertNotNull(webpBytes);

		Path outputPath = Paths.get("output/test-result.webp");
		Files.createDirectories(outputPath.getParent());
		Files.write(outputPath, webpBytes);
	}
}