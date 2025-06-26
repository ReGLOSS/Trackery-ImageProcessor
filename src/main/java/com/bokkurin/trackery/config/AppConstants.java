package com.bokkurin.trackery.config;

import software.amazon.awssdk.regions.Region;

/**
 * packageName    : com.bokkurin.trackery.config
 * fileName       : AppConstants
 * author         : durururuk
 * date           : 25. 6. 26.
 * description    : 상수를 담당하는 스태틱 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 26.      durururuk     최초 생성
 * 25. 6. 26.      durururuk     AppConstants로 설정값 분리
 */
public class AppConstants {
	private AppConstants() {
	}

	// AWS 설정
	public static final Region AWS_REGION = Region.of(
		System.getenv().get("AWS_REGION")
	);
	public static final String SOURCE_BUCKET = System.getenv("SOURCE_BUCKET");
	public static final String DESTINATION_BUCKET = System.getenv("DESTINATION_BUCKET");

	// S3 경로 패턴
	public static final String SOURCE_PREFIX = System.getenv("SOURCE_PREFIX");
	public static final String DESTINATION_ORIGINAL_PATH = System.getenv("DESTINATION_ORIGINAL_PATH");
	public static final String DESTINATION_THUMBNAIL_PATH = System.getenv("DESTINATION_THUMBNAIL_PATH");

	// 이미지 처리 설정
	public static final int THUMBNAIL_SIZE = 300;
	public static final String OUTPUT_FORMAT = "webp";

	// 지원하는 이미지 확장자
	public static final String[] SUPPORTED_EXTENSIONS = {
		".jpg", ".jpeg", ".png", "webp"
	};
}
