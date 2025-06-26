package com.bokkurin.trackery.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * packageName    : com.bokkurin.trackery.config
 * fileName       : AwsConfiguration
 * author         : durururuk
 * date           : 25. 6. 26.
 * description    : AWS 설정 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 26.      durururuk     최초 생성
 */
public class AwsConfiguration {
    private static final Region AWS_REGION = Region.of(
        System.getenv().getOrDefault("AWS_REGION", "ap-northeast-2")
    );
    private static final String SOURCE_BUCKET = System.getenv("SOURCE_BUCKET");
    private static final String DESTINATION_BUCKET = System.getenv("DESTINATION_BUCKET");
    
    private static S3Client s3Client;

    /**
     * S3 클라이언트 인스턴스 반환하는 메서드
     * @return S3 클라이언트
     */
    public static S3Client getS3Client() {
       if (s3Client == null) {
          s3Client = S3Client.builder()
             .region(AWS_REGION)
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
    
    // Getter 메서드들
    public static String getSourceBucket() {
        return SOURCE_BUCKET;
    }
    
    public static String getDestinationBucket() {
        return DESTINATION_BUCKET;
    }
}
