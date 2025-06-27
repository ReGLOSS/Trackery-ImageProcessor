package com.bokkurin.trackery.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bokkurin.trackery.config.AppConstants;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * packageName    : com.bokkurin.trackery.service
 * fileName       : S3ActionService
 * author         : durururuk
 * date           : 25. 6. 26.
 * description    : S3 업로드/다운로드 기능 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 26.		durururuk		최초 생성
 */
public class S3ActionService {
	private static final Logger logger = LoggerFactory.getLogger(S3ActionService.class);
	
	private final S3Client s3Client;
	
	public S3ActionService(S3Client s3Client) {
		this.s3Client = s3Client;
	}
	
	/**
	 * S3에서 이미지를 다운로드하여 바이트 배열로 반환
	 * @param bucketName S3 버킷 이름
	 * @param key S3 객체 키
	 * @return 이미지 바이트 배열
	 * @throws IOException 다운로드 실패 시
	 */
	public byte[] downloadImage(String bucketName, String key) throws IOException {
		logger.info("S3에서 이미지 다운로드 시작 - 버킷: {}, 키: {}", bucketName, key);
		
		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.build();
			
			ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
			byte[] imageBytes = s3Object.readAllBytes();
			
			logger.info("S3 이미지 다운로드 완료 - 크기: {} bytes", imageBytes.length);
			return imageBytes;
			
		} catch (NoSuchKeyException e) {
			String errorMsg = String.format("S3 객체를 찾을 수 없습니다 - 버킷: %s, 키: %s", bucketName, key);
			logger.error(errorMsg, e);
			throw new IOException(errorMsg, e);

		} catch (S3Exception e) {
			String errorMsg = String.format("S3 오류 발생 - 버킷: %s, 키: %s, 에러코드: %s", 
				bucketName, key, e.awsErrorDetails().errorCode());
			logger.error(errorMsg, e);
			throw new IOException(errorMsg, e);
			
		} catch (IOException e) {
			String errorMsg = String.format("이미지 읽기 실패 - 버킷: %s, 키: %s", bucketName, key);
			logger.error(errorMsg, e);
			throw new IOException(errorMsg, e);
		}
	}

	/**
	 * S3에 이미지를 업로드
	 * @param bucketName S3 버킷 이름
	 * @param key S3 객체 키
	 * @param imageBytes 업로드할 이미지 바이트 배열
	 * @param contentType 컨텐츠 타입 (예: "image/webp")
	 * @throws IOException 업로드 실패 시
	 */
	public void uploadImage(String bucketName, String key, byte[] imageBytes, String contentType) throws IOException {
		logger.info("S3에 이미지 업로드 시작 - 버킷: {}, 키: {}, 크기: {} bytes", bucketName, key, imageBytes.length);

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.contentType(contentType)
				.contentLength((long) imageBytes.length)
				.build();

			RequestBody requestBody = RequestBody.fromBytes(imageBytes);

			PutObjectResponse response = s3Client.putObject(putObjectRequest, requestBody);

			logger.info("S3 이미지 업로드 완료 - ETag: {}", response.eTag());

		} catch (S3Exception e) {
			String errorMsg = String.format("S3 업로드 오류 - 버킷: %s, 키: %s, 에러코드: %s",
				bucketName, key, e.awsErrorDetails().errorCode());
			logger.error(errorMsg, e);
			throw new IOException(errorMsg, e);

		} catch (Exception e) {
			String errorMsg = String.format("이미지 업로드 실패 - 버킷: %s, 키: %s", bucketName, key);
			logger.error(errorMsg, e);
			throw new IOException(errorMsg, e);
		}
	}

	/**
	 * 원본 WebP 이미지를 업로드
	 */
	public void uploadOriginalWebP(String originalKey, byte[] webpBytes) throws IOException {
		String destinationKey = "original/" + changeExtensionToWebP(originalKey);
		uploadImage(AppConstants.DESTINATION_BUCKET, destinationKey, webpBytes, "image/webp");
	}

	/**
	 * 썸네일 이미지를 업로드
	 */
	public void uploadThumbnail(String originalKey, byte[] thumbnailBytes) throws IOException {
		String destinationKey = "thumbnail/" + changeExtensionToWebP(originalKey);
		uploadImage(AppConstants.DESTINATION_BUCKET, destinationKey, thumbnailBytes, "image/webp");
	}

	/**
	 * 파일 확장자를 .webp로 변경
	 */
	private String changeExtensionToWebP(String key) {
		int lastDotIndex = key.lastIndexOf('.');
		if (lastDotIndex > 0) {
			return key.substring(0, lastDotIndex) + ".webp";
		}
		return key + ".webp";
	}
}
