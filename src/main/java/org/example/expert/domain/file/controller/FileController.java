package org.example.expert.domain.file.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.file.dto.request.PresignedGetUrlRequest;
import org.example.expert.domain.file.dto.request.PresignedPutUrlRequest;
import org.example.expert.domain.file.dto.response.PresignedGetUrlResponse;
import org.example.expert.domain.file.dto.response.PresignedPutUrlResponse;
import org.example.expert.domain.file.service.FileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/presigned/upload")
    public PresignedPutUrlResponse createPresignedPutUrl(
        @AuthenticationPrincipal AuthUser authUser,
        @RequestBody PresignedPutUrlRequest request
    ) {

        return fileService.createPresignedPutUrl(
            authUser,
            request.getDomain(),
            request.getFilename()
        );
    }

    @PostMapping("/presigned/download")
    public PresignedGetUrlResponse createPresignedGetUrl(
        @RequestBody PresignedGetUrlRequest request
    ) {

        return fileService.createPresignedGetUrl(request.getFileKey());
    }
}
