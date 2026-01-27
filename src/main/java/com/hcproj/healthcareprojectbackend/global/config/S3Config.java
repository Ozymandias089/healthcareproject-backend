package com.hcproj.healthcareprojectbackend.global.config;

import com.hcproj.healthcareprojectbackend.global.config.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 연동을 위한 클라이언트 설정 클래스.
 *
 * <p>
 * <b>제공 Bean</b>
 * <ul>
 *   <li>{@link S3Client} : S3 객체 업로드/다운로드</li>
 *   <li>{@link S3Presigner} : Presigned URL 생성</li>
 * </ul>
 *
 * <p>
 * <b>주의 사항</b>
 * <ul>
 *   <li>운영 환경에서는 Access Key / Secret Key 대신 IAM Role 사용 권장</li>
 *   <li>현재 구성은 로컬 및 단일 계정 환경을 전제로 함</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final S3Properties s3Properties;

    /**
     * AWS 자격증명 Provider 생성.
     *
     * @return StaticCredentialsProvider
     */
    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        s3Properties.getAccessKey(),
                        s3Properties.getSecretKey()
                )
        );
    }

    /**
     * S3 Client Bean.
     *
     * @return S3Client
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(credentialsProvider())
                .build();
    }

    /**
     * S3 Presigner Bean.
     *
     * <p>
     * Presigned URL을 생성하여
     * 클라이언트가 직접 S3에 업로드/다운로드하도록 할 때 사용된다.
     *
     * @return S3Presigner
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(credentialsProvider())
                .build();
    }
}