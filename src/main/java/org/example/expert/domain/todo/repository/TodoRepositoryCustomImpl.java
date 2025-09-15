package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
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
    public Page<TodoSearchResponse> searchWithRelationCounts(
        String keyword, LocalDate startDate,
        LocalDate endDate, String managerName, Pageable pageable
    ) {

        BooleanBuilder conditions = new BooleanBuilder();
        conditions.and(containsKeyword(keyword));
        conditions.and(goeCreatedAt(startDate));
        conditions.and(loeCreatedAt(endDate));
        conditions.and(containsManagerName(managerName));

        List<TodoSearchResponse> content = queryFactory
            .select(new QTodoSearchResponse(
                todo.title,
                manager.countDistinct(),
                comment.countDistinct()
            ))
            .from(todo)
            .leftJoin(manager).on(manager.todo.eq(todo))
            .leftJoin(comment).on(comment.todo.eq(todo))
            .where(conditions)
            .groupBy(todo.id)
            .orderBy(todo.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () ->
            Optional.ofNullable(queryFactory
                .select(todo.count())
                .from(todo)
                .where(conditions)
                .fetchOne()
            ).orElse(0L)
        );
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
            ? todo.createdAt.lt(endDate.plusDays(1).atStartOfDay())
            : null;
    }

    private BooleanExpression containsManagerName(String nickname) {

        if (!StringUtils.hasText(nickname)) {
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
