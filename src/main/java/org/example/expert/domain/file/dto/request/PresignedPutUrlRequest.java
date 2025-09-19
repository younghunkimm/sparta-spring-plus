package org.example.expert.domain.file.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.file.enums.FileDomain;

@Getter
@NoArgsConstructor
public class PresignedPutUrlRequest {

    private FileDomain domain;
    private String filename;
}
