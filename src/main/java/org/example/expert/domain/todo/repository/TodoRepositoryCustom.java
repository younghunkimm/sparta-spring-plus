package org.example.expert.domain.todo.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodoRepositoryCustom {

    Optional<Todo> findByIdWithUser(Long todoId);

    Page<TodoSearchResponse> searchWithRelationCounts(
        String keyword,
        LocalDate startDate,
        LocalDate endDate,
        String managerName,
        Pageable pageable
    );
}
