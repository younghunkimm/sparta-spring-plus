package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.QTodoSearchResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
            .selectFrom(todo)
            .leftJoin(todo.user, user).fetchJoin()
            .where(todo.id.eq(todoId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTitleAndCount(
        String keyword, LocalDate startDate,
        LocalDate endDate, String managerName, Pageable pageable
    ) {
        List<TodoSearchResponse> content = queryFactory
            .select(new QTodoSearchResponse(
                todo.title,
                JPAExpressions
                    .select(manager.id.count())
                    .from(manager)
                    .where(
                        manager.todo.eq(todo)
                    ),
                JPAExpressions
                    .select(comment.id.count())
                    .from(comment)
                    .where(
                        comment.todo.eq(todo)
                    )
            ))
            .from(todo)
            .where(
                containsKeyword(keyword),
                goeCreatedAt(startDate),
                loeCreatedAt(endDate),
                containsManagerName(managerName)
            )
            .orderBy(todo.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = Optional.ofNullable(queryFactory
            .select(todo.count())
            .from(todo)
            .where(
                containsKeyword(keyword),
                goeCreatedAt(startDate),
                loeCreatedAt(endDate),
                containsManagerName(managerName)
            )
            .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression containsKeyword(String keyword) {

        return StringUtils.hasText(keyword)
            ? todo.title.containsIgnoreCase(keyword)
            : null;
    }

    private BooleanExpression goeCreatedAt(LocalDate startDate) {

        return startDate != null
            ? todo.createdAt.goe(LocalDateTime.of(startDate, LocalTime.MIN))
            : null;
    }

    private BooleanExpression loeCreatedAt(LocalDate endDate) {

        return endDate != null
            ? todo.createdAt.loe(LocalDateTime.of(endDate, LocalTime.MAX))
            : null;
    }

    private BooleanExpression containsManagerName(String nickname) {

        if (nickname == null) {
            return null;
        }

        return JPAExpressions
            .selectOne()
            .from(manager)
            .where(
                manager.todo.eq(todo),
                manager.user.nickname.contains(nickname)
            )
            .exists();
    }
}
