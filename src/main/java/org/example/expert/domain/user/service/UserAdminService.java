package org.example.expert.domain.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserBulkRepository;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final UserBulkRepository userBulkRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changeUserRole(long userId, UserRoleChangeRequest userRoleChangeRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidRequestException("User not found"));
        user.updateRole(UserRole.of(userRoleChangeRequest.getRole()));
    }

    @Async
    @Transactional
    public void bulkInsertUsersAsync(int count) {
        // 시작 로그 출력
        log.info("{}명의 사용자 대량 생성을 시작합니다.", count);

        // 청크 사이즈(1000)와, User 객체 리스트 초기화
        // OutOfMemory 방지를 위해 청크 단위로 나누어 삽입
        final int CHUNK_SIZE = 1_000;
        List<User> userChunk = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode("password123"); // 공통 비밀번호 사용

        // 반복 시작
        for (int i = 0; i < count; i++) {
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String email = String.format("user%s@example.com", uuid);
            String nickname = String.format("user-%s", uuid);

            User user = new User(email, encodedPassword, nickname, UserRole.ROLE_USER);
            userChunk.add(user);

            // 청크가 가득 찼을 때 데이터베이스에 삽입
            if (userChunk.size() == CHUNK_SIZE) {
                // Bulk insert 실행
                userBulkRepository.bulkInsert(userChunk);

                // 리스트를 비워서 메모리 해제
                userChunk.clear();
                log.info("{}명 생성 완료...", (i + 1));

                // Throttling 처리
                try {
                    log.info("100ms 대기 중...");
                    Thread.sleep(100); // 100ms 대기
                } catch (InterruptedException e) {
                    log.warn("스레드 대기 중 인터럽트 발생: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                    return; // 인터럽트 발생 시 작업 중단
                }
            }
        }

        // 마지막 남은 청크 처리
        if (!userChunk.isEmpty()) {
            // Bulk insert 실행
            userBulkRepository.bulkInsert(userChunk);
            log.info("마지막 {}명 생성 완료.", userChunk.size());
        }

        log.info("총 {}명의 사용자 대량 생성을 완료했습니다.", count);
    }
}
