package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS S3 연동을 위한 설정 프로퍼티.
 *
 * <p>
 * <b>설정 예</b>
 * <pre>
 * aws.s3.bucket=my-bucket
 * aws.s3.region=ap-northeast-2
 * aws.s3.access-key=xxxx
 * aws.s3.secret-key=yyyy
 * aws.s3.presigned-url-expiration=10
 * </pre>
 *
 * <p>
 * <b>주의</b>
 * <ul>
 *   <li>Access Key / Secret Key는 반드시 환경 변수 또는 Vault로 관리</li>
 *   <li>운영 환경에서는 IAM Role 사용 권장</li>
 * </ul>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {

    /** S3 버킷 이름 */
    private String bucket;

    /** AWS 리전 */
    private String region;

    /** Access Key */
    private String accessKey;

    /** Secret Key */
    private String secretKey;

    /**
     * Presigned URL 만료 시간 (분)
     * 기본값: 10분
     */
    private int presignedUrlExpiration = 10;
}