package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.enums.LogStatus;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(
        Long userId,
        String httpMethod,
        String requestUrl,
        String methodName,
        LogStatus status,
        String errorMessage
    ) {

        Log log = Log.of(userId, httpMethod, requestUrl, methodName, status, errorMessage);
        logRepository.save(log);
    }
}
