package com.pedra.wollenlab.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedra.wollenlab.domain.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    val conversations = conversationRepository.getAllConversations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // La creación de conversaciones se maneja en ChatViewModel cuando se envía el primer mensaje

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(id)
        }
    }
}

