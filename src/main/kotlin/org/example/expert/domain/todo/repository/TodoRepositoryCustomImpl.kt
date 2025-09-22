package org.example.expert.domain.todo.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.example.expert.domain.comment.entity.QComment.comment
import org.example.expert.domain.manager.entity.QManager.manager
import org.example.expert.domain.todo.dto.response.QTodoSearchResponse
import org.example.expert.domain.todo.dto.response.TodoSearchResponse
import org.example.expert.domain.todo.entity.QTodo.todo
import org.example.expert.domain.todo.entity.Todo
import org.example.expert.domain.user.entity.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import java.time.LocalDate

class TodoRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : TodoRepositoryCustom {

    override fun findByIdWithUser(todoId: Long): Todo? {
        return queryFactory
            .selectFrom(todo)
            .leftJoin(todo.user, user).fetchJoin()
            .where(todo.id.eq(todoId))
            .fetchOne()
    }

    override fun searchWithRelationCounts(
        keyword: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        managerName: String?,
        pageable: Pageable,
    ): Page<TodoSearchResponse> {

        val conditions = BooleanBuilder()
            .and(containsKeyword(keyword))
            .and(goeCreatedAt(startDate))
            .and(ltCreatedAt(endDate))
            .and(containsManagerName(managerName))

        val content = queryFactory
            .select(
                QTodoSearchResponse(
                    todo.title,
                    JPAExpressions
                        .select(manager.user.id.countDistinct())
                        .from(manager)
                        .where(manager.todo.eq(todo)),
                    JPAExpressions
                        .select(comment.id.count())
                        .from(comment)
                        .where(comment.todo.eq(todo))
                )
            )
            .from(todo)
            .where(conditions)
            .orderBy(todo.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = queryFactory
            .select(todo.count())
            .from(todo)
            .where(conditions)

        /*
         * 함수의 마지막 파라미터가 람다 표현식인 경우
         * 코틀린 문법인 후행 람다(Trailing Lambda) 사용 가능
         * 괄호`()` 바깥으로 빼서 `{}`로 작성 가능
         */
        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun containsKeyword(keyword: String?): BooleanExpression? {

        /**
         * 1. X keyword가 null이면 null 반환
         * 2. O keyword가 null이 아니면 takeIf 블록 실행
         * 3. O takeIf 블록에서 keyword가 blank가 아니면 keyword 반환
         * 4. X keyword가 blank이면 null 반환
         * 5. O let 블록 실행
         * 6. O let 블록에서 todo.title에 keyword가 포함되어 있으면 true 반환
         * 7. O 포함되어 있지 않으면 false 반환
         */
        return keyword
            ?.takeIf { it.isNotBlank() }
            ?.let { todo.title.containsIgnoreCase(it) }
    }

    private fun goeCreatedAt(startDate: LocalDate?): BooleanExpression? {

        return startDate?.let { todo.createdAt.goe(it.atStartOfDay()) }
    }

    private fun ltCreatedAt(endDate: LocalDate?): BooleanExpression? {

        return endDate?.let { todo.createdAt.lt(it.plusDays(1).atStartOfDay()) }
    }

    private fun containsManagerName(managerName: String?): BooleanExpression? {

        return managerName
            ?.takeIf { it.isNotBlank() }
            ?.let {
                JPAExpressions
                    .selectFrom(manager)
                    .where(
                        manager.todo.eq(todo)
                            .and(manager.user.nickname.containsIgnoreCase(it))
                    )
                    .exists()
            }
    }
}