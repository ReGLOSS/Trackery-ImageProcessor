package com.bokkurin.trackery.service;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bokkurin.trackery.config.AppConstants;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;

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
 * 25. 7. 16.		durururuk		이미지 방향을 받아와서 맞게 수정하는 작업 추가
 */
public class ImageProcessService {
	private static final Logger logger = LoggerFactory.getLogger(ImageProcessService.class);

	/**
	 * 원본 이미지를 WebP로 변환하는 메서드
	 * @param imageBytes 원본 이미지 바이트
	 * @return 변환된 이미지 바이트
	 * @throws IOException 이미지를 불러오지 못했거나 변환에 실패했을 때 발생하는 예외
	 */
	public byte[] convertToWebP(byte[] imageBytes) throws IOException {
		logger.info("WebP 변환 시작");

		BufferedImage originalImage = getOriginalImage(imageBytes);

		return convertBufferedImageToWebP(originalImage, "원본");
	}

	/**
	 * 원본 이미지를 썸네일용 작은 이미지로 변환하는 메서드
	 * 길이, 높이 중 짧은 부분을 300px로 잡고 비율을 맞춰서 리사이징합니다.
	 * @param imageBytes 원본 이미지 바이트 배열
	 * @return 변환된 이미지 바이트 배열
	 * @throws IOException 이미지를 불러오지 못했거나 변환에 실패했을 때 발생하는 예외
	 */
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

	/**
	 * 원본 이미지 바이트를 불러오는 메서드
	 * @param imageBytes 원본 이미지 바이트 배열
	 * @return 생성된 BufferedImage 객체
	 * @throws IOException 지원하지 않는 이미지 형식이거나 데이터가 손상되었을 경우 발생합니다.
	 */
	private BufferedImage getOriginalImage(byte[] imageBytes) throws IOException {
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
		if (originalImage == null) {
			throw new IOException("원본 이미지 불러오기 실패");
		}

		return applyExifOrientation(originalImage, imageBytes);
	}

	/**
	 * BufferedImage를 WebP 형식의 바이트 배열로 변환하는 메서드
	 * @param image      변환할 BufferedImage 객체
	 * @param logContext 로깅 시 사용할 컨텍스트 문자열 (예: "원본", "썸네일")
	 * @return WebP로 변환된 이미지의 바이트 배열
	 * @throws IOException 이미지 변환에 실패했을 경우 발생합니다.
	 */
	private byte[] convertBufferedImageToWebP(BufferedImage image, String logContext) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		boolean result = ImageIO.write(image, AppConstants.OUTPUT_FORMAT, outputStream);

		if (!result) {
			throw new IOException(logContext + " WebP 출력 실패");
		}

		logger.info("{} WebP 변환 완료", logContext);
		return outputStream.toByteArray();
	}

	/**
	 * EXIF 방향 정보를 적용하여 이미지를 올바른 방향으로 회전시키는 메서드
	 * @param image 원본 BufferedImage
	 * @param imageBytes 원본 이미지 바이트 배열 (EXIF 정보 읽기용)
	 * @return 방향이 보정된 BufferedImage
	 */
	private BufferedImage applyExifOrientation(BufferedImage image, byte[] imageBytes) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageBytes));
			ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

			if (directory != null && directory.hasTagName(ExifDirectoryBase.TAG_ORIENTATION)) {
				int orientation = directory.getInt(ExifDirectoryBase.TAG_ORIENTATION);
				logger.info("EXIF 방향 정보 감지: {}", orientation);
				return rotateImageByOrientation(image, orientation);
			}
		} catch (ImageProcessingException | IOException | MetadataException e) {
			logger.warn("EXIF 방향 정보 처리 중 오류 발생: {}", e.getMessage());
		}
		return image;
	}

	/**
	 * EXIF 방향 값에 따라 이미지를 회전시키는 메서드
	 * @param image 원본 BufferedImage
	 * @param orientation EXIF 방향 값 (1-8)
	 * @return 회전된 BufferedImage
	 */
	private BufferedImage rotateImageByOrientation(BufferedImage image, int orientation) {
		AffineTransform transform = new AffineTransform();
		
		switch (orientation) {
			case 1:
				// 정상 방향 (회전 없음)
				return image;
			case 2:
				// 수평 반전
				transform.scale(-1.0, 1.0);
				transform.translate(-image.getWidth(), 0);
				break;
			case 3:
				// 180도 회전
				transform.translate(image.getWidth(), image.getHeight());
				transform.rotate(Math.PI);
				break;
			case 4:
				// 수직 반전
				transform.scale(1.0, -1.0);
				transform.translate(0, -image.getHeight());
				break;
			case 5:
				// 90도 반시계방향 회전 + 수평 반전
				transform.rotate(-Math.PI / 2);
				transform.scale(-1.0, 1.0);
				break;
			case 6:
				// 90도 시계방향 회전
				transform.translate(image.getHeight(), 0);
				transform.rotate(Math.PI / 2);
				break;
			case 7:
				// 90도 시계방향 회전 + 수평 반전
				transform.scale(-1.0, 1.0);
				transform.translate(-image.getHeight(), 0);
				transform.translate(0, image.getWidth());
				transform.rotate(3 * Math.PI / 2);
				break;
			case 8:
				// 90도 반시계방향 회전
				transform.translate(0, image.getWidth());
				transform.rotate(-Math.PI / 2);
				break;
			default:
				// 알 수 없는 방향값
				logger.warn("알 수 없는 EXIF 방향 값: {}", orientation);
				return image;
		}

		return applyTransformation(image, transform, orientation);
	}

	/**
	 * AffineTransform을 적용하여 이미지를 변환하는 메서드
	 * @param image 원본 BufferedImage
	 * @param transform 적용할 AffineTransform
	 * @param orientation EXIF 방향 값 (로깅용)
	 * @return 변환된 BufferedImage
	 */
	private BufferedImage applyTransformation(BufferedImage image, AffineTransform transform, int orientation) {
		// 회전 후 이미지 크기 계산
		int newWidth = image.getWidth();
		int newHeight = image.getHeight();
		
		if (orientation == 6 || orientation == 8) {
			// 90도 회전 시 가로세로 바뀜
			newWidth = image.getHeight();
			newHeight = image.getWidth();
		}

		BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());
		Graphics2D g2d = rotatedImage.createGraphics();
		
		// 고품질 렌더링 설정
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setTransform(transform);
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		logger.info("EXIF 방향 보정 완료: {} -> 정상방향", orientation);
		return rotatedImage;
	}

}
