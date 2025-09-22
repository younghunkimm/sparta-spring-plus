package org.example.expert.domain.todo.service

import org.example.expert.client.WeatherClient
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.common.exception.InvalidRequestException
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.dto.response.TodoResponse
import org.example.expert.domain.todo.dto.response.TodoSaveResponse
import org.example.expert.domain.todo.dto.response.TodoSearchResponse
import org.example.expert.domain.todo.entity.Todo
import org.example.expert.domain.todo.repository.TodoRepository

import org.example.expert.domain.user.dto.response.UserResponse
import org.example.expert.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class TodoService(
    private val todoRepository: TodoRepository,
    private val weatherClient: WeatherClient,
) {

    @Transactional
    fun saveTodo(
        authUser: AuthUser,
        todoSaveRequest: TodoSaveRequest,
    ): TodoSaveResponse {

        val user = User.fromAuthUser(authUser)
        val weather = weatherClient.todayWeather

        val newTodo = Todo(
            title = todoSaveRequest.title,
            contents = todoSaveRequest.contents,
            weather = weather,
            user = user,
        )
        val savedTodo = todoRepository.save(newTodo)

        return TodoSaveResponse(
            id = savedTodo.id,
            title = savedTodo.title,
            contents = savedTodo.contents,
            weather = savedTodo.weather,
            user = UserResponse(user.id, user.email),
        )
    }

    fun getTodos(
        weather: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        page: Int,
        size: Int,
    ): Page<TodoResponse> {

        val pageable = PageRequest.of(page - 1, size)
        val todos = todoRepository.searchWithUser(weather, startDate, endDate, pageable)

        return todos.map {
            TodoResponse(
                id = it.id,
                title = it.title,
                contents = it.contents,
                weather = it.weather,
                user = UserResponse(it.user.id, it.user.email),
                createdAt = it.createdAt,
                modifiedAt = it.modifiedAt,
            )
        }
    }

    fun getTodo(todoId: Long): TodoResponse {

        val todo = todoRepository.findByIdWithUser(todoId)
            ?: throw InvalidRequestException("Todo not found")

        val user = todo.user;

        return TodoResponse(
            id = todo.id,
            title = todo.title,
            contents = todo.contents,
            weather = todo.weather,
            user = UserResponse(user.id, user.email),
            createdAt = todo.createdAt,
            modifiedAt = todo.modifiedAt,
        )
    }

    fun searchWithRelationCounts(
        keyword: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        managerName: String?,
        page: Int,
        size: Int,
    ): Page<TodoSearchResponse> {

        val pageable = PageRequest.of(page - 1, size)

        return todoRepository.searchWithRelationCounts(
            keyword = keyword,
            startDate = startDate,
            endDate = endDate,
            managerName = managerName,
            pageable = pageable,
        )
    }
}