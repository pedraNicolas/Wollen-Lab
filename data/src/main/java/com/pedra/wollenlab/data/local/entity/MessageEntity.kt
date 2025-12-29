package com.pedra.wollenlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pedra.wollenlab.model.MessageRole
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

