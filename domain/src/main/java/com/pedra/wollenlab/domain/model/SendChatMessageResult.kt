package com.pedra.wollenlab.domain.model

import com.pedra.wollenlab.model.Message

data class SendChatMessageResult(
    val conversationId: String,
    val assistantMessage: Message,
    val shouldUpdateTitle: Boolean
)

