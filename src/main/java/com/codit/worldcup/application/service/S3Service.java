package com.codit.worldcup.application.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

	private final S3Client s3Client;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Value("${cloud.aws.region.static}")
	private String region;

	// 생성자 주입
	public S3Service(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	/**
	 * S3에 파일을 업로드하고, 공개 접근 URL을 반환합니다.
	 * @param multipartFile 업로드할 파일
	 * @param directory 업로드할 S3 버킷 내부 디렉토리 (예: "worldcup/candidates/")
	 * @return S3 파일 URL
	 */
	public String uploadFile(MultipartFile multipartFile, String directory) {
		// 1. 파일 이름 생성 (UUID를 사용하여 중복 방지)
		String originalFilename = multipartFile.getOriginalFilename();
		String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
		String uniqueFileName = directory + UUID.randomUUID().toString() + fileExtension;

		try {
			// 2. S3 업로드 요청 생성
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(uniqueFileName) // S3 객체 키
				.contentType(multipartFile.getContentType())
				.contentLength(multipartFile.getSize())
				.build();

			// 3. 파일 업로드 실행
			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
				multipartFile.getInputStream(), multipartFile.getSize()));

			// 4. 공개 접근 가능한 URL 생성
			// 💡 S3 버킷 설정에서 Public Read Access가 활성화되어 있어야 합니다.
			// URL 형식: https://[버킷 이름].s3.[리전].amazonaws.com/[객체 키]
			return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, uniqueFileName);

		} catch (IOException e) {
			throw new RuntimeException("S3 파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}