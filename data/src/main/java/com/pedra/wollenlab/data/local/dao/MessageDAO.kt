package com.pedra.wollenlab.data.local.dao

import androidx.room.*
import com.pedra.wollenlab.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDAO {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesByConversationId(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    suspend fun getMessagesByConversationIdSync(conversationId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: String)
}

