package com.bokkurin.trackery.service;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

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

	@Test
	void testCreateThumbnail() throws IOException {
		InputStream imageStream = getClass().getResourceAsStream("/sample-image/image.jpg");
		assertNotNull(imageStream);
		byte[] imageBytes = imageStream.readAllBytes();

		ImageProcessService imageProcessService = new ImageProcessService();
		byte[] thumbnailBytes = imageProcessService.createThumbnail(imageBytes);

		assertNotNull(thumbnailBytes);

		BufferedImage thumbnailImage = ImageIO.read(new ByteArrayInputStream(thumbnailBytes));
		assertNotNull(thumbnailImage);
		
		int width = thumbnailImage.getWidth();
		int height = thumbnailImage.getHeight();

		assertTrue(width >= 300 || height >= 300, 
			String.format("썸네일 크기가 너무 작습니다. 실제 크기: %dx%d", width, height));

		int minSize = Math.min(width, height);
		assertEquals(300, minSize, 
			String.format("최소 크기가 300px이어야 합니다. 실제 크기: %dx%d", width, height));

		Path outputPath = Paths.get("output/test-thumbnail.webp");
		Files.createDirectories(outputPath.getParent());
		Files.write(outputPath, thumbnailBytes);
	}
}