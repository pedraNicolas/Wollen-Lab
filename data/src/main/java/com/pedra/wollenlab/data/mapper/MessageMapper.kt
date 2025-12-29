package com.pedra.wollenlab.data.mapper

import com.pedra.wollenlab.data.local.entity.MessageEntity
import com.pedra.wollenlab.model.Message

fun MessageEntity.toDomain() = Message(
    id = id,
    content = content,
    role = role,
    timestamp = createdAt
)

fun Message.toEntity(conversationId: String) = MessageEntity(
    id = id,
    conversationId = conversationId,
    role = role,
    content = content,
    createdAt = timestamp
)

