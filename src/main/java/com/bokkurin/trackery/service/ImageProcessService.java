package com.bokkurin.trackery.service;

import java.awt.*;
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
 * 25. 6. 26.		durururuk		webp 변환 메서드 작성, 테스트용 샘플 이미지 추가
 */
public class ImageProcessService {
	private static final Logger logger = LoggerFactory.getLogger(ImageProcessService.class);

	//WebP로 변환
	public byte[] convertToWebP(byte[] imageBytes) throws IOException {
		logger.info("WebP 변환 시작");

		BufferedImage originalImage = getOriginalImage(imageBytes);

		return convertBufferedImageToWebP(originalImage, "원본");
	}

	//썸네일 생성 (300x300급)
	public byte[] createThumbnail(byte[] imageBytes) throws IOException {
		logger.info("썸네일 생성 시작");

		BufferedImage originalImage = getOriginalImage(imageBytes);

		// 원본 이미지 크기
		int originalWidth = originalImage.getWidth();
		int originalHeight = originalImage.getHeight();

		//300x300 급으로 스케일링
		double widthRatio = (double)originalWidth / AppConstants.THUMBNAIL_SIZE;
		double heightRatio = (double)originalHeight / AppConstants.THUMBNAIL_SIZE;
		double ratio = Math.min(widthRatio, heightRatio);

		int scaledWidth = (int)(originalWidth / ratio);
		int scaledHeight = (int)(originalHeight / ratio);

		//스케일링된 이미지 생성
		BufferedImage thumbnailImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = thumbnailImage.createGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		graphics.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		graphics.dispose();

		return convertBufferedImageToWebP(thumbnailImage, "썸네일");
	}

	private BufferedImage getOriginalImage(byte[] imageBytes) throws IOException {
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
		if (originalImage == null) {
			throw new IOException("원본 이미지 불러오기 실패");
		}

		return originalImage;
	}

	private byte[] convertBufferedImageToWebP(BufferedImage image, String logContext) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		boolean result = ImageIO.write(image, AppConstants.OUTPUT_FORMAT, outputStream);

		if (!result) {
			throw new IOException(logContext + " WebP 출력 실패");
		}

		logger.info("{} WebP 변환 완료", logContext);
		return outputStream.toByteArray();
	}

}
