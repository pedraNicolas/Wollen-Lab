package com.pedra.wollenlab.presentation.state

import com.pedra.wollenlab.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isInputEnabled: Boolean
        get() = !isLoading
}

