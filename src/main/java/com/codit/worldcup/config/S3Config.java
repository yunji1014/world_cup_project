package com.codit.worldcup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

	@Value("${cloud.aws.credentials.accessKey}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secretKey}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	/**
	 * S3Client 빈 생성 및 등록
	 */
	@Bean
	public S3Client s3Client() {
		// AWS 자격 증명 설정
		AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

		return S3Client.builder()
			.region(Region.of(region)) // 리전 설정
			.credentialsProvider(StaticCredentialsProvider.create(credentials)) // 자격 증명 설정
			.build();
	}
}