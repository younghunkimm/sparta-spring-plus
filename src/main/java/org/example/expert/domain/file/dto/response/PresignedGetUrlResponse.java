package org.example.expert.domain.file.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedGetUrlResponse {

    private final String fileKey;
    private final String presignedUrl;
}
