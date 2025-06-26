package com.bokkurin.trackery.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bokkurin.trackery.config.AppConstants;

/**
 * packageName    : com.bokkurin.trackery.service
 * fileName       : ImageProcessService
 * author         : durururuk
 * date           : 25. 6. 26.
 * description    : 이미지 처리하는 기능 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 26.		durururuk		최초 생성
 */
public class ImageProcessService {
	private static final Logger logger = LoggerFactory.getLogger(ImageProcessService.class);

	//WebP로 변환
	public byte[] convertToWebP(byte[] imageBytes) throws IOException {
		logger.info("WebP 변환 시작");

		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
		if (originalImage == null) {
			throw new IOException("이미지 불러오기 실패");
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		boolean result = ImageIO.write(originalImage, AppConstants.OUTPUT_FORMAT, outputStream);

		if (!result) {
			throw new IOException("WebP 변환 실패");
		}

		logger.info("WebP 변환 완료");
		return outputStream.toByteArray();
	}
}
