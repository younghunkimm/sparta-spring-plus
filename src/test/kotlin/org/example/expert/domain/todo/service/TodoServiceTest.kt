package org.example.expert.domain.todo.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.expert.client.WeatherClient
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.common.entity.Timestamped
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.entity.Todo
import org.example.expert.domain.todo.repository.TodoRepository
import org.example.expert.domain.user.entity.User


import org.example.expert.domain.user.enums.UserRole
import java.time.LocalDateTime


class TodoServiceTest : BehaviorSpec({
    val todoRepository = mockk<TodoRepository>()
    val weatherClient = mockk<WeatherClient>()
    val todoService = TodoService(todoRepository, weatherClient)

    Given("Todo 생성을 위한 정보가 주어졌을 때") {
        val authUser = AuthUser(
            id = 1L,
            email = "test@test.com",
            nickname = "TestUser",
            userRole = UserRole.ROLE_USER
        )
        val request = TodoSaveRequest(title = "Test Title", contents = "Test Contents")
        val weather = "맑음"
        val user = User.fromAuthUser(authUser)
        val todo = Todo(
            title = request.title,
            contents = request.contents,
            weather = weather,
            user = user,
        )

        every { weatherClient.todayWeather } returns weather
        every { todoRepository.save(any()) } returns todo

        When("saveTodo 메서드를 호출하면") {
            val result = todoService.saveTodo(authUser, request)

            Then("결과가 예상과 같아야 한다") {
                result.title shouldBe request.title
                result.contents shouldBe request.contents
                result.weather shouldBe weather
                result.user.id shouldBe authUser.id
            }

            Then("todoRepository의 save 메서드가 한 번 호출되어야 한다") {
                verify(exactly = 1) { todoRepository.save(any()) }
            }
        }
    }

    Given("Todo 단건 조회를 위한 ID가 주어졌을 때") {
        val todoId = 1L
        val authUser = AuthUser(
            id = 1L,
            email = "test@example.com",
            nickname = "TestUser",
            userRole = UserRole.ROLE_USER
        )
        val user = User.fromAuthUser(authUser)
        val todo = Todo(
            id = todoId,
            title = "Test Title",
            contents = "Test Contents",
            weather = "맑음",
            user = user,
        ).apply {
            val createdAtField = Timestamped::class.java.getDeclaredField("createdAt")
            val modifiedAtField = Timestamped::class.java.getDeclaredField("modifiedAt")
            createdAtField.isAccessible = true
            modifiedAtField.isAccessible = true
            createdAtField.set(this, LocalDateTime.now())
            modifiedAtField.set(this, LocalDateTime.now())
        }

        every { todoRepository.findByIdWithUser(any()) } returns todo

        When("getTodo 메서드를 호출하면") {
            val result = todoService.getTodo(todoId)

            Then("결과가 예상과 같아야 한다") {
                result.id shouldBe todoId
                result.title shouldBe todo.title
                result.user.id shouldBe user.id
            }

            Then("todoRepository의 findByIdWithUser 메서드가 한 번 호출되어야 한다") {
                verify(exactly = 1) { todoRepository.findByIdWithUser(todoId) }
            }
        }
    }
})