package org.example.expert.domain.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URL;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.file.dto.response.PresignedGetUrlResponse;
import org.example.expert.domain.file.dto.response.PresignedPutUrlResponse;
import org.example.expert.domain.file.enums.FileDomain;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(fileService, "prefix", "test-prefix");
    }

    @Nested
    class CreatePresignedPutUrl {

        @Test
            // 실제 AWS에서 발급받는 Presigned URL을 검증하는 것이 아닌
            // FileService가 올바른 형식의 URL을 생성하는지 검증하는 테스트
        void Presigned_PUT_URL_생성에_성공한다() throws Exception {
            // === given ===
            // 메서드 호출에 필요한 기본 데이터 생성
            AuthUser authUser = new AuthUser(1L, "test@test.com", "test", UserRole.ROLE_USER);
            FileDomain domain = FileDomain.PROFILE;
            String fileName = "test.jpg";

            /*
            s3Presigner 가짜 객체가 FileService로부터 전달받는 요청 안에서 실제 `fileKey`를 꺼내 보고,
            그것을 기반으로 URL을 동적으로 생성하여 반환하도록 설정

            FileService가 s3Presigner를 호출할 때, 진짜 AWS로 가지 못하게 가로챈다.
            */
            given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willAnswer(invocation -> {
                    // 메서드 호출 시 사용된 인자를 가져온다.
                    PutObjectPresignRequest presignRequest = invocation.getArgument(0);
                    // 인자에서 fileKey 값을 추출한다.
                    String key = presignRequest.putObjectRequest().key();
                    // 추출한 key를 이용해 동적으로 URL을 생성한다.
                    URL url = new URL("https://test-bucket.s3.region.amazonaws.com/" + key);
                    // 최종 반환 객체를 만들기 위해 mock 객체를 생성한다.
                    PresignedPutObjectRequest mockPresignedRequest = mock(
                        PresignedPutObjectRequest.class);
                    // mock 객체의 url() 메서드가 호출되면 위에서 생성한 URL을 반환하도록 설정한다.
                    given(mockPresignedRequest.url()).willReturn(url);
                    // mock 객체를 반환한다.
                    return mockPresignedRequest;
                });

            // === when ===
            PresignedPutUrlResponse response = fileService.createPresignedPutUrl(authUser, domain,
                fileName);

            // === then ===
            assertThat(response.getFileKey()).startsWith(
                "test-prefix/" + domain.getDirectory() + "/1/");
            assertThat(response.getFileKey()).endsWith("/" + fileName);

            String[] parts = response.getFileKey().split("/");
            assertThat(parts).hasSize(5);

            String expectedUrl =
                "https://test-bucket.s3.region.amazonaws.com/" + response.getFileKey();
            assertThat(response.getPresignedUrl()).isEqualTo(expectedUrl);
        }
    }

    @Nested
    class CreatePresignedGetUrl {

        @Test
        void Presigned_GET_URL_생성에_성공한다() throws Exception {
            // given
            String fileKey = "test-prefix/profile/1/some-uuid/test.jpg";
            String expectedUrl = "https://test-bucket.s3.amazonaws.com/test-prefix/profile/1/some-uuid/test.jpg";

            PresignedGetObjectRequest presignedGetObjectRequest = mock(
                PresignedGetObjectRequest.class);
            given(presignedGetObjectRequest.url()).willReturn(new URL(expectedUrl));
            given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).willReturn(
                presignedGetObjectRequest);

            // when
            PresignedGetUrlResponse response = fileService.createPresignedGetUrl(fileKey);

            // then
            assertThat(response.getPresignedUrl()).isEqualTo(expectedUrl);
            assertThat(response.getFileKey()).isEqualTo(fileKey);
        }
    }


    @Nested
    class DeleteFile {

        @Test
        void 파일_삭제에_성공한다() {
            // given
            String fileKey = "test-prefix/profile/1/some-uuid/test.jpg";

            // when
            fileService.deleteFile(fileKey);

            // then
            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }
    }
}