package com.pedra.wollenlab.domain.usecase

import com.pedra.wollenlab.domain.model.SendChatMessageResult
import com.pedra.wollenlab.domain.repository.ChatRepository
import com.pedra.wollenlab.domain.repository.ConversationRepository
import com.pedra.wollenlab.model.Message
import com.pedra.wollenlab.model.MessageRole
import java.util.UUID
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(
        conversationId: String?,
        userMessage: Message
    ): Result<SendChatMessageResult> {
        return try {
            // Crear conversación si no existe
            val finalConversationId = conversationId ?: conversationRepository.createConversation().id

            // Obtener mensajes existentes ANTES de guardar el nuevo mensaje
            val existingMessages = conversationRepository.getMessagesByConversationIdSync(finalConversationId)
            val isFirstMessage = existingMessages.isEmpty()

            // Guardar mensaje del usuario
            conversationRepository.saveMessage(finalConversationId, userMessage)

            // Preparar mensajes para enviar (con resumen si es necesario)
            val messagesToSend = prepareMessagesForSending(existingMessages, userMessage)

            // Enviar mensaje a la IA
            val response = chatRepository.sendMessage(messagesToSend)
                .getOrElse { error -> return Result.failure(error) }

            // Crear mensaje del asistente
            val assistantMessage = Message(
                id = UUID.randomUUID().toString(),
                content = response,
                role = MessageRole.ASSISTANT
            )

            // Guardar mensaje del asistente
            conversationRepository.saveMessage(finalConversationId, assistantMessage)

            // Actualizar título si es el primer mensaje
            if (isFirstMessage) {
                updateConversationTitle(finalConversationId, userMessage)
            }

            Result.success(SendChatMessageResult(finalConversationId, assistantMessage, isFirstMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun prepareMessagesForSending(
        existingMessages: List<Message>,
        userMessage: Message
    ): List<Message> {
        // Si hay muchos mensajes, usar resumen
        return if (shouldUseSummary(existingMessages.size)) {
            val summary = conversationRepository.generateSummary(existingMessages)
            listOf(
                Message(
                    id = UUID.randomUUID().toString(),
                    content = "${ConversationConstants.SUMMARY_PREFIX}$summary",
                    role = MessageRole.SYSTEM
                ),
                userMessage
            )
        } else {
            existingMessages + userMessage
        }
    }

    private fun shouldUseSummary(messageCount: Int): Boolean {
        return messageCount > ConversationConstants.SUMMARY_THRESHOLD
    }

    private suspend fun updateConversationTitle(conversationId: String, userMessage: Message) {
        val title = generateTitleFromMessage(userMessage.content)
        conversationRepository.getConversationById(conversationId)?.let { conversation ->
            conversationRepository.updateConversation(
                conversation.copy(title = title)
            )
        }
    }

    private fun generateTitleFromMessage(messageContent: String): String {
        return messageContent
            .take(ConversationConstants.MAX_TITLE_LENGTH)
            .ifEmpty { ConversationConstants.DEFAULT_TITLE }
    }
}

