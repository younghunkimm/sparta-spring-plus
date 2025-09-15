package org.example.expert.domain.log.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.log.enums.LogStatus;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String requestUrl;

    @Column(nullable = false)
    private String methodName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LogStatus status;

    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Log(
        Long userId,
        String httpMethod,
        String requestUrl,
        String methodName,
        LogStatus status,
        String errorMessage
    ) {

        this.userId = userId;
        this.httpMethod = httpMethod;
        this.requestUrl = requestUrl;
        this.methodName = methodName;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }

    public static Log of(
        Long userId,
        String httpMethod,
        String requestUrl,
        String methodName,
        LogStatus status,
        String errorMessage
    ) {

        return new Log(userId, httpMethod, requestUrl, methodName, status, errorMessage);
    }
}
