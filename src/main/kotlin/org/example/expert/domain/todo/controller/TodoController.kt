package org.example.expert.domain.todo.controller

import jakarta.validation.Valid
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.dto.response.TodoResponse
import org.example.expert.domain.todo.dto.response.TodoSaveResponse
import org.example.expert.domain.todo.dto.response.TodoSearchResponse
import org.example.expert.domain.todo.service.TodoService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
class TodoController(
    private val todoService: TodoService,
) {

    @PostMapping("/todos")
    fun saveTodo(
        @AuthenticationPrincipal authUser: AuthUser,
        @Valid @RequestBody todoSaveRequest: TodoSaveRequest,
    ): ResponseEntity<TodoSaveResponse> =
        ResponseEntity.ok(todoService.saveTodo(authUser, todoSaveRequest))

    @GetMapping("/todos")
    fun getTodos(
        @RequestParam(required = false) weather: String?,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Page<TodoResponse>> =
        ResponseEntity.ok(
            todoService.getTodos(
                weather,
                startDate,
                endDate,
                page,
                size,
            )
        )

    @GetMapping("/todos/{todoId}")
    fun getTodo(
        @PathVariable todoId: Long,
    ): ResponseEntity<TodoResponse> =
        ResponseEntity.ok(todoService.getTodo(todoId))

    @GetMapping("/todos/search")
    fun searchWithRelationCounts(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?,
        @RequestParam(required = false) managerName: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Page<TodoSearchResponse>> =
        ResponseEntity.ok(
            todoService.searchWithRelationCounts(
                keyword,
                startDate,
                endDate,
                managerName,
                page,
                size
            )
        )
}