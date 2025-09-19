package org.example.expert.domain.file.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.file.dto.response.PresignedGetUrlResponse;
import org.example.expert.domain.file.dto.response.PresignedPutUrlResponse;
import org.example.expert.domain.file.enums.FileDomain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${storage.prefix}")
    private String prefix;

    public PresignedPutUrlResponse createPresignedPutUrl(
        AuthUser authUser,
        FileDomain domain,
        String fileName
    ) {

        String fileKey = String.format("%s/%s/%d/%s/%s",
            prefix,
            domain.getDirectory(),
            authUser.getId(),
            UUID.randomUUID(),
            fileName
        );

        String presignedUrl = generatePresignedPutUrl(fileKey);

        return PresignedPutUrlResponse.builder()
            .presignedUrl(presignedUrl)
            .fileKey(fileKey)
            .build();
    }

    public PresignedGetUrlResponse createPresignedGetUrl(String fileKey) {

        String presignedUrl = generatePresignedGetUrl(fileKey);
        
        return PresignedGetUrlResponse.builder()
            .presignedUrl(presignedUrl)
            .fileKey(fileKey)
            .build();
    }

    public String getPresignedUrl(String fileKey) {
        return generatePresignedGetUrl(fileKey);
    }

    private String generatePresignedPutUrl(String fileKey) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .putObjectRequest(putObjectRequest)
            .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    private String generatePresignedGetUrl(String fileKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(fileKey)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(getObjectRequest)
            .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public void deleteFile(String fileKey) {

        s3Client.deleteObject(builder ->
            builder
                .bucket(bucket)
                .key(fileKey)
                .build()
        );
    }
}