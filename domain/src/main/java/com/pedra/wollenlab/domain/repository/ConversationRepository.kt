package com.pedra.wollenlab.domain.repository

import com.pedra.wollenlab.domain.model.Conversation
import com.pedra.wollenlab.model.Message
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    suspend fun getConversationById(id: String): Conversation?
    suspend fun createConversation(title: String? = null): Conversation
    suspend fun updateConversation(conversation: Conversation)
    suspend fun deleteConversation(id: String)
    
    fun getMessagesByConversationId(conversationId: String): Flow<List<Message>>
    suspend fun getMessagesByConversationIdSync(conversationId: String): List<Message>
    suspend fun saveMessage(conversationId: String, message: Message)
    suspend fun saveMessages(conversationId: String, messages: List<Message>)
    
    suspend fun generateSummary(messages: List<Message>): String
}

