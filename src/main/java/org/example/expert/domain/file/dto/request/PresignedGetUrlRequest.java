package org.example.expert.domain.file.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignedGetUrlRequest {

    private String fileKey;
}
