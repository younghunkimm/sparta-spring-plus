package org.example.expert.domain.file.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedPutUrlResponse {

    private final String fileKey;
    private final String presignedUrl;
}
