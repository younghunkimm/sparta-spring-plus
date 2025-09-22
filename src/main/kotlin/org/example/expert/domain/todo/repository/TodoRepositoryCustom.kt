package org.example.expert.domain.todo.repository

import org.example.expert.domain.todo.dto.response.TodoSearchResponse
import org.example.expert.domain.todo.entity.Todo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface TodoRepositoryCustom {

    fun findByIdWithUser(todoId: Long): Todo?

    fun searchWithRelationCounts(
        keyword: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        managerName: String?,
        pageable: Pageable,
    ): Page<TodoSearchResponse>
}