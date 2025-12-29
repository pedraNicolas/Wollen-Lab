package com.pedra.wollenlab.domain.usecase

import com.pedra.wollenlab.domain.model.Conversation
import com.pedra.wollenlab.domain.model.SendChatMessageResult
import com.pedra.wollenlab.domain.repository.ChatRepository
import com.pedra.wollenlab.domain.repository.ConversationRepository
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

class SendChatMessageUseCaseTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var conversationRepository: ConversationRepository
    private lateinit var useCase: SendChatMessageUseCase

    @Before
    fun setup() {
        chatRepository = mockk()
        conversationRepository = mockk()
        useCase = SendChatMessageUseCase(chatRepository, conversationRepository)
    }

    @Test
    fun `invoke should create conversation when conversationId is null`() = runTest {
        // Given
        val conversationId = "new-conversation-id"
        val userMessage = createUserMessage("Hola")
        val assistantResponse = "Hola! ¿En qué te puedo ayudar?"
        val conversation = createConversation(conversationId)

        coEvery { conversationRepository.createConversation() } returns conversation
        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns emptyList()
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { conversationRepository.getConversationById(conversationId) } returns conversation
        coEvery { conversationRepository.updateConversation(any()) } returns Unit
        coEvery { chatRepository.sendMessage(any()) } returns Result.success(assistantResponse)

        // When
        val result = useCase(null, userMessage)

        // Then
        assertTrue(result.isSuccess)
        val successResult = result.getOrNull()!!
        assertEquals(conversationId, successResult.conversationId)
        assertEquals(assistantResponse, successResult.assistantMessage.content)
        assertEquals(MessageRole.ASSISTANT, successResult.assistantMessage.role)
        assertTrue(successResult.shouldUpdateTitle)
        coVerify { conversationRepository.createConversation() }
        coVerify { conversationRepository.updateConversation(any()) }
    }

    @Test
    fun `invoke should use existing conversation when conversationId is provided`() = runTest {
        // Given
        val conversationId = "existing-conversation-id"
        val userMessage = createUserMessage("Segundo mensaje")
        val assistantResponse = "Respuesta del asistente"
        val existingMessages = listOf(
            createUserMessage("Primer mensaje"),
            createAssistantMessage("Primera respuesta")
        )

        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns existingMessages
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { chatRepository.sendMessage(any()) } returns Result.success(assistantResponse)

        // When
        val result = useCase(conversationId, userMessage)

        // Then
        assertTrue(result.isSuccess)
        val successResult = result.getOrNull()!!
        assertEquals(conversationId, successResult.conversationId)
        assertFalse(successResult.shouldUpdateTitle)
        coVerify(exactly = 0) { conversationRepository.createConversation() }
        coVerify(exactly = 0) { conversationRepository.updateConversation(any()) }
    }

    @Test
    fun `invoke should use summary when message count exceeds threshold`() = runTest {
        // Given
        val conversationId = "conversation-id"
        val userMessage = createUserMessage("Nuevo mensaje")
        val assistantResponse = "Respuesta"
        
        // Crear más mensajes que el threshold (10)
        val existingMessages = (1..11).map { index ->
            if (index % 2 == 0) {
                createUserMessage("Mensaje usuario $index")
            } else {
                createAssistantMessage("Mensaje asistente $index")
            }
        }

        val summary = "Resumen de la conversación anterior"

        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns existingMessages
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { conversationRepository.generateSummary(existingMessages) } returns summary
        coEvery { chatRepository.sendMessage(any()) } returns Result.success(assistantResponse)

        // When
        val result = useCase(conversationId, userMessage)

        // Then
        assertTrue(result.isSuccess)
        coVerify { conversationRepository.generateSummary(existingMessages) }
        coVerify {
            chatRepository.sendMessage(match { messages ->
                messages.size == 2 &&
                messages[0].role == MessageRole.SYSTEM &&
                messages[0].content.startsWith(ConversationConstants.SUMMARY_PREFIX) &&
                messages[1] == userMessage
            })
        }
    }

    @Test
    fun `invoke should not use summary when message count is below threshold`() = runTest {
        // Given
        val conversationId = "conversation-id"
        val userMessage = createUserMessage("Nuevo mensaje")
        val assistantResponse = "Respuesta"
        
        // Crear menos mensajes que el threshold (10)
        val existingMessages = listOf(
            createUserMessage("Mensaje 1"),
            createAssistantMessage("Respuesta 1")
        )

        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns existingMessages
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { chatRepository.sendMessage(any()) } returns Result.success(assistantResponse)

        // When
        val result = useCase(conversationId, userMessage)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { conversationRepository.generateSummary(any()) }
        coVerify {
            chatRepository.sendMessage(match { messages ->
                messages.size == 3 &&
                messages == existingMessages + userMessage
            })
        }
    }

    @Test
    fun `invoke should generate title from first message`() = runTest {
        // Given
        val conversationId = "new-conversation-id"
        val userMessage = createUserMessage("Este es un mensaje de prueba para el título")
        val assistantResponse = "Respuesta"
        val conversation = createConversation(conversationId)

        coEvery { conversationRepository.createConversation() } returns conversation
        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns emptyList()
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { conversationRepository.getConversationById(conversationId) } returns conversation
        coEvery { conversationRepository.updateConversation(any()) } returns Unit
        coEvery { chatRepository.sendMessage(any()) } returns Result.success(assistantResponse)

        // When
        val result = useCase(null, userMessage)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            conversationRepository.updateConversation(
                match { it.title == "Este es un mensaje de prueba para el título" }
            )
        }
    }

    @Test
    fun `invoke should truncate long title to max length`() = runTest {
        // Given
        val conversationId = "new-conversation-id"
        val longMessage = "A".repeat(100) // Mensaje muy largo
        val userMessage = createUserMessage(longMessage)
        val assistantResponse = "Respuesta"
        val conversation = createConversation(conversationId)

        coEvery { conversationRepository.createConversation() } returns conversation
        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns emptyList()
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { conversationRepository.getConversationById(conversationId) } returns conversation
        coEvery { conversationRepository.updateConversation(any()) } returns Unit
        coEvery { chatRepository.sendMessage(any()) } returns Result.success(assistantResponse)

        // When
        val result = useCase(null, userMessage)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            conversationRepository.updateConversation(
                match { 
                    it.title?.length == ConversationConstants.MAX_TITLE_LENGTH
                }
            )
        }
    }

    @Test
    fun `invoke should use default title when message is empty`() = runTest {
        // Given
        val conversationId = "new-conversation-id"
        val userMessage = createUserMessage("")
        val assistantResponse = "Respuesta"
        val conversation = createConversation(conversationId)

        coEvery { conversationRepository.createConversation() } returns conversation
        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns emptyList()
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { conversationRepository.getConversationById(conversationId) } returns conversation
        coEvery { conversationRepository.updateConversation(any()) } returns Unit
        coEvery { chatRepository.sendMessage(any()) } returns Result.success(assistantResponse)

        // When
        val result = useCase(null, userMessage)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            conversationRepository.updateConversation(
                match { it.title == ConversationConstants.DEFAULT_TITLE }
            )
        }
    }

    @Test
    fun `invoke should return failure when chat repository fails`() = runTest {
        // Given
        val conversationId = "conversation-id"
        val userMessage = createUserMessage("Hola")
        val error = Exception("Network error")

        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns emptyList()
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { chatRepository.sendMessage(any()) } returns Result.failure(error)

        // When
        val result = useCase(conversationId, userMessage)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val conversationId = "conversation-id"
        val userMessage = createUserMessage("Hola")
        val error = RuntimeException("Database error")

        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } throws error

        // When
        val result = useCase(conversationId, userMessage)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `invoke should save both user and assistant messages`() = runTest {
        // Given
        val conversationId = "conversation-id"
        val userMessage = createUserMessage("Hola")
        val assistantResponse = "Respuesta del asistente"

        coEvery { conversationRepository.getMessagesByConversationIdSync(conversationId) } returns emptyList()
        coEvery { conversationRepository.saveMessage(any(), any()) } returns Unit
        coEvery { conversationRepository.getConversationById(conversationId) } returns createConversation(conversationId)
        coEvery { conversationRepository.updateConversation(any()) } returns Unit
        coEvery { chatRepository.sendMessage(any()) } returns Result.success(assistantResponse)

        // When
        val result = useCase(conversationId, userMessage)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { conversationRepository.saveMessage(conversationId, any()) }
    }

    // Helper functions
    private fun createUserMessage(content: String): Message {
        return Message(
            id = "user-msg-id",
            content = content,
            role = MessageRole.USER
        )
    }

    private fun createAssistantMessage(content: String): Message {
        return Message(
            id = "assistant-msg-id",
            content = content,
            role = MessageRole.ASSISTANT
        )
    }

    private fun createConversation(id: String, title: String? = null): Conversation {
        return Conversation(
            id = id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            title = title
        )
    }
}

