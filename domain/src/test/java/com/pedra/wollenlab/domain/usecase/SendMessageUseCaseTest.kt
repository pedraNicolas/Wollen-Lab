package com.pedra.wollenlab.domain.usecase

import com.pedra.wollenlab.domain.repository.ChatRepository
import com.pedra.wollenlab.model.Message
import com.pedra.wollenlab.model.MessageRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var useCase: SendMessageUseCase

    @Before
    fun setup() {
        chatRepository = mockk()
        useCase = SendMessageUseCase(chatRepository)
    }

    @Test
    fun `invoke should return success when repository succeeds`() = runTest {
        // Given
        val messages = listOf(
            Message(
                id = "msg-1",
                content = "Hola",
                role = MessageRole.USER
            )
        )
        val expectedResponse = "Hola! ¿En qué te puedo ayudar?"

        coEvery { chatRepository.sendMessage(messages) } returns Result.success(expectedResponse)

        // When
        val result = useCase(messages)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        coVerify { chatRepository.sendMessage(messages) }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Given
        val messages = listOf(
            Message(
                id = "msg-1",
                content = "Hola",
                role = MessageRole.USER
            )
        )
        val error = Exception("Network error")

        coEvery { chatRepository.sendMessage(messages) } returns Result.failure(error)

        // When
        val result = useCase(messages)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify { chatRepository.sendMessage(messages) }
    }

    @Test
    fun `invoke should pass multiple messages to repository`() = runTest {
        // Given
        val messages = listOf(
            Message(
                id = "msg-1",
                content = "Primer mensaje",
                role = MessageRole.USER
            ),
            Message(
                id = "msg-2",
                content = "Segundo mensaje",
                role = MessageRole.USER
            ),
            Message(
                id = "msg-3",
                content = "Respuesta",
                role = MessageRole.ASSISTANT
            )
        )
        val expectedResponse = "Respuesta del asistente"

        coEvery { chatRepository.sendMessage(messages) } returns Result.success(expectedResponse)

        // When
        val result = useCase(messages)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        coVerify { chatRepository.sendMessage(messages) }
    }

    @Test
    fun `invoke should handle empty message list`() = runTest {
        // Given
        val messages = emptyList<Message>()
        val expectedResponse = "Respuesta"

        coEvery { chatRepository.sendMessage(messages) } returns Result.success(expectedResponse)

        // When
        val result = useCase(messages)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        coVerify { chatRepository.sendMessage(messages) }
    }

    @Test
    fun `invoke should propagate different error types`() = runTest {
        // Given
        val messages = listOf(
            Message(
                id = "msg-1",
                content = "Hola",
                role = MessageRole.USER
            )
        )
        val runtimeError = RuntimeException("Runtime error")
        val illegalStateError = IllegalStateException("Illegal state")

        // Test RuntimeException
        coEvery { chatRepository.sendMessage(messages) } returns Result.failure(runtimeError)
        var result = useCase(messages)
        assertTrue(result.isFailure)
        assertEquals(runtimeError, result.exceptionOrNull())

        // Test IllegalStateException
        coEvery { chatRepository.sendMessage(messages) } returns Result.failure(illegalStateError)
        result = useCase(messages)
        assertTrue(result.isFailure)
        assertEquals(illegalStateError, result.exceptionOrNull())
    }

    @Test
    fun `invoke should handle messages with different roles`() = runTest {
        // Given
        val messages = listOf(
            Message(
                id = "msg-1",
                content = "Mensaje del sistema",
                role = MessageRole.SYSTEM
            ),
            Message(
                id = "msg-2",
                content = "Mensaje del usuario",
                role = MessageRole.USER
            ),
            Message(
                id = "msg-3",
                content = "Mensaje del asistente",
                role = MessageRole.ASSISTANT
            )
        )
        val expectedResponse = "Respuesta"

        coEvery { chatRepository.sendMessage(messages) } returns Result.success(expectedResponse)

        // When
        val result = useCase(messages)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        coVerify { chatRepository.sendMessage(messages) }
    }
}

