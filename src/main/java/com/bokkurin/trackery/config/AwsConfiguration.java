package com.bokkurin.trackery.config;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * packageName    : com.bokkurin.trackery.config
 * fileName       : AwsConfiguration
 * author         : durururuk
 * date           : 25. 6. 26.
 * description    : AWS 서비스 클라이언트 설정을 담당하는 Configuration 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 26.      durururuk     최초 생성
 * 25. 6. 26.      durururuk     AppConstants로 설정값 분리
 */
public class AwsConfiguration {
    private static S3Client s3Client;

    private AwsConfiguration() {
    }

    /**
     * S3 클라이언트 인스턴스 반환하는 메서드
     * Lambda 환경에서는 IAM Role의 자격증명을 자동으로 사용합니다.
     * @return S3 클라이언트
     */
    @SuppressWarnings("java:S6242")
    public static S3Client getS3Client() {
       if (s3Client == null) {
          s3Client = S3Client.builder()
             .region(AppConstants.AWS_REGION)
             .build();
       }
       return s3Client;
    }

    /**
     * 람다 함수 호출이 끝나면 S3 리소스 반환하는 메서드
     */
    public static void cleanup() {
       if (s3Client != null) {
          s3Client.close();
          s3Client = null;
       }
    }
}
