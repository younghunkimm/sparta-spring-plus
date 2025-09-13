package org.example.expert.domain.todo.repository;

import java.time.LocalDate;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    @Query("""
            SELECT t
            FROM Todo t
            LEFT JOIN FETCH t.user u
            WHERE (COALESCE(:weather, '') = '' OR t.weather LIKE CONCAT('%', :weather, '%'))
                AND (:startDate IS NULL OR FUNCTION('DATE', t.modifiedAt) >= :startDate)
                AND (:endDate IS NULL OR FUNCTION('DATE', t.modifiedAt) <= :endDate)
            ORDER BY t.modifiedAt DESC
        """)
    Page<Todo> searchWithUser(
        @Param("weather") String weather,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
    
}
