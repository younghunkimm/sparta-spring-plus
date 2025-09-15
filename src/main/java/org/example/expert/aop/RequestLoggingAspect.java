package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.log.enums.LogStatus;
import org.example.expert.domain.log.service.LogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestLoggingAspect {

    private final LogService logService;

    @Around("@annotation(org.example.expert.domain.log.annotation.RequestLogging)")
    public Object logDBRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest httpRequest = getHttpRequest();

        if (httpRequest == null) {
            log.warn("httpRequest is null");
            return joinPoint.proceed();
        }

        AuthUser authUser = getAuthUser();

        Long userId = authUser.getId();
        String httpMethod = httpRequest.getMethod();
        String requestUrl = httpRequest.getRequestURI();
        String methodName = joinPoint.getSignature().getName();

        Object result;
        LogStatus logStatus = null;
        String errorMessage = null;
        try {
            result = joinPoint.proceed();
            logStatus = LogStatus.SUCCESS;
        } catch (Exception e) {
            logStatus = LogStatus.FAILURE;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            logService.saveLog(userId, httpMethod, requestUrl, methodName, logStatus, errorMessage);
        }

        return result;
    }

    private HttpServletRequest getHttpRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attr != null ? attr.getRequest() : null;
    }

    private AuthUser getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthUser) authentication.getPrincipal();
    }
}
