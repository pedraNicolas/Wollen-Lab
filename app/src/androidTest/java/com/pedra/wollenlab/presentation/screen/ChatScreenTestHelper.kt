package com.pedra.wollenlab.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedra.wollenlab.R
import com.pedra.wollenlab.model.MessageRole
import com.pedra.wollenlab.presentation.state.ChatUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Versión de prueba de ChatScreen que acepta StateFlows directamente
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestChatScreen(
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    uiState: StateFlow<ChatUiState>,
    conversations: StateFlow<List<com.pedra.wollenlab.domain.model.Conversation>>,
    onSendMessage: (String) -> Unit,
    onUpdateInputText: (String) -> Unit,
    onCreateNewConversation: () -> Unit,
    onLoadConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit
) {
    val chatUiState by uiState.collectAsStateWithLifecycle()
    val conversationsList by conversations.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(chatUiState.messages.size) {
        if (chatUiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatUiState.messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationsDrawer(
                conversations = conversationsList,
                onConversationClick = { conversationId ->
                    onLoadConversation(conversationId)
                    coroutineScope.launch { drawerState.close() }
                },
                onNewConversationClick = {
                    onCreateNewConversation()
                    coroutineScope.launch { drawerState.close() }
                },
                onDeleteConversation = { conversationId ->
                    onDeleteConversation(conversationId)
                },
                darkTheme = darkTheme
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Icon(
                            painter = painterResource(
                                id = if (darkTheme) R.drawable.ic_wollen_lab_white else R.drawable.ic_wollen_lab_black
                            ),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(120.dp, 16.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.chat_menu)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onThemeChange(!darkTheme) }) {
                            Icon(
                                imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (darkTheme) stringResource(R.string.chat_theme_light) else stringResource(R.string.chat_theme_dark)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Área de contenido (mensajes o bienvenida)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Lista de mensajes
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            bottom = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chatUiState.messages) { message ->
                            MessageBubble(message = message)
                        }

                        if (chatUiState.isLoading) {
                            item {
                                LoadingIndicator()
                            }
                        }
                    }

                    // Mensaje de bienvenida (solo cuando no hay mensajes y no está cargando)
                    if (chatUiState.messages.isEmpty() && !chatUiState.isLoading) {
                        WelcomeMessage(
                            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
                        )
                    }
                }

                // Input y botón de envío
                ChatInput(
                    text = chatUiState.inputText,
                    onTextChange = onUpdateInputText,
                    onSendClick = { onSendMessage(chatUiState.inputText) },
                    enabled = chatUiState.isInputEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                )
            }
        }
    }
}

