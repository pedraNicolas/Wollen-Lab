package com.pedra.wollenlab.data.mapper

import com.pedra.wollenlab.data.local.entity.ConversationEntity
import com.pedra.wollenlab.domain.model.Conversation

fun ConversationEntity.toDomain() = Conversation(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    title = title
)

fun Conversation.toEntity() = ConversationEntity(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    title = title
)

