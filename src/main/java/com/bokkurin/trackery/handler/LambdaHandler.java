package com.bokkurin.trackery.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.bokkurin.trackery.config.AwsConfiguration;
import com.bokkurin.trackery.service.S3ActionService;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * packageName    : com.bokkurin.trackery.handler
 * fileName       : LambdaHandler
 * author         : durururuk
 * date           : 25. 6. 26.
 * description    : 람다 함수를 실행할 수 있게 하는 람다 핸들러
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 26.		durururuk		최초 생성
 */
public class LambdaHandler implements RequestHandler<S3Event, String> {
	private static final Logger logger = LoggerFactory.getLogger(LambdaHandler.class);

	private final S3ActionService s3ActionService;

	public LambdaHandler() {
		S3Client s3Client = AwsConfiguration.getS3Client();
		this.s3ActionService = new S3ActionService(s3Client);
	}

	@Override
	public String handleRequest(S3Event s3Event, Context context) {
		logger.info("이미지 후처리 실행");

		try {
			for (S3EventNotification.S3EventNotificationRecord notificationRecord : s3Event.getRecords()) {
				String bucketName = notificationRecord.getS3().getBucket().getName();
				String objectKey = notificationRecord.getS3().getObject().getKey();

				logger.info("처리할 파일 - 버킷: {}, 키: {}", bucketName, objectKey);

				byte[] imageBytes = s3ActionService.downloadImage(bucketName, objectKey);
				logger.info("다운로드 성공 - 파일 크기: {} bytes", imageBytes.length);

			}

			return "SUCCESS";
		} catch (IOException e) {
			logger.error("이미지 처리 실패", e);
			throw new RuntimeException("Lambda 실행 실패", e);
		}
	}
}
