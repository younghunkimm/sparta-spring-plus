package org.example.expert.domain.todo.dto.request

import jakarta.validation.constraints.NotBlank

/**
 * `@NoArgsConstructor` 필요없는 이유
 * - Jackson이 코틀린의 주 생성자를 직접 사용하여 객체를 생성하므로 `@NoArgsConstructor`가 필요 없음
 * - Spring Boot + Kotlin 사용 시 `jackson-module-kotlin` 라이브러리가 이 기능을 자동 활성화
 */
data class TodoSaveRequest(
    /**
     * Property
     * - 생성자 파라미터, 필드(field), 게터(getter) 등을 모두 포함하는 '프로퍼티(Property)'라는 복합적인 개념
     * - 따라서, 그냥 `@NotBlank` 어노테이션을 붙이면 코틀린 컴파일러는 어디에 붙여야할지 모름
     * - `@field:NotBlank` 처럼 명시적으로 '필드'에 붙여야 한다고 지정 (Use-site Target)
     */
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val contents: String,
)