package org.example.expert.domain.todo.dto.response

import com.querydsl.core.annotations.QueryProjection

data class TodoSearchResponse @QueryProjection constructor(
    val title: String,
    val managerCount: Long,
    val commentCount: Long,
)