package org.example.expert.domain.todo.repository

import org.example.expert.domain.todo.entity.Todo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface TodoRepository : JpaRepository<Todo, Long>, TodoRepositoryCustom {

    @Query(
        """
        SELECT t
        FROM Todo t
        LEFT JOIN FETCH t.user u
        WHERE (COALESCE(:weather, '') = '' OR t.weather LIKE CONCAT('%', :weather, '%'))
            AND (:startDate IS NULL OR FUNCTION('DATE', t.modifiedAt) >= :startDate)
            AND (:endDate IS NULL OR FUNCTION('DATE', t.modifiedAt) <= :endDate)
        ORDER BY t.modifiedAt DESC
        """
    )
    fun searchWithUser(
        @Param("weather") weather: String?,
        @Param("startDate") startDate: LocalDate?,
        @Param("endDate") endDate: LocalDate?,
        pageable: Pageable,
    ): Page<Todo>
}