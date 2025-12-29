package com.pedra.wollenlab.data.repository

import com.pedra.wollenlab.data.local.dao.ConversationDAO
import com.pedra.wollenlab.data.local.dao.MessageDAO
import com.pedra.wollenlab.data.local.entity.ConversationEntity
import com.pedra.wollenlab.data.mapper.toDomain
import com.pedra.wollenlab.data.mapper.toEntity
import com.pedra.wollenlab.domain.model.Conversation
import com.pedra.wollenlab.domain.repository.ConversationRepository
import com.pedra.wollenlab.model.Message
import com.pedra.wollenlab.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDAO,
    private val messageDao: MessageDAO
) : ConversationRepository {

    override fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getConversationById(id: String): Conversation? {
        return conversationDao.getConversationById(id)?.toDomain()
    }

    override suspend fun createConversation(title: String?): Conversation {
        val entity = ConversationEntity(title = title)
        conversationDao.insertConversation(entity)
        return entity.toDomain()
    }

    override suspend fun updateConversation(conversation: Conversation) {
        conversationDao.updateConversation(conversation.toEntity())
    }

    override suspend fun deleteConversation(id: String) {
        conversationDao.deleteConversationById(id)
        messageDao.deleteMessagesByConversationId(id)
    }

    override fun getMessagesByConversationId(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesByConversationId(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMessagesByConversationIdSync(conversationId: String): List<Message> {
        return messageDao.getMessagesByConversationIdSync(conversationId).map { it.toDomain() }
    }

    override suspend fun saveMessage(conversationId: String, message: Message) {
        messageDao.insertMessage(message.toEntity(conversationId))
        conversationDao.getConversationById(conversationId)?.let { conversation ->
            conversationDao.updateConversation(
                conversation.copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

    override suspend fun saveMessages(conversationId: String, messages: List<Message>) {
        messageDao.insertMessages(messages.map { it.toEntity(conversationId) })
        conversationDao.getConversationById(conversationId)?.let { conversation ->
            conversationDao.updateConversation(
                conversation.copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

    override suspend fun generateSummary(messages: List<Message>): String {
        // Resumen simple: primeros 3 mensajes del usuario y últimos 2 mensajes
        val userMessages = messages.filter { it.role == MessageRole.USER }
        val assistantMessages = messages.filter { it.role == MessageRole.ASSISTANT }
        
        val summaryParts = mutableListOf<String>()
        
        // Primeros 3 mensajes del usuario
        userMessages.take(3).forEach { message ->
            summaryParts.add("Usuario: ${message.content.take(100)}")
        }
        
        // Últimos 2 mensajes (usuario y asistente)
        val lastMessages = messages.takeLast(2)
        if (lastMessages.isNotEmpty()) {
            summaryParts.add("...")
            lastMessages.forEach { message ->
                val role = if (message.role == MessageRole.USER) "Usuario" else "Asistente"
                summaryParts.add("$role: ${message.content.take(100)}")
            }
        }
        
        return summaryParts.joinToString("\n")
    }
}

