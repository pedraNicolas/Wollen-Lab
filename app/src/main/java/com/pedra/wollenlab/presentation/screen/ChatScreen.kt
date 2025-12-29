package com.pedra.wollenlab.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedra.wollenlab.R
import com.pedra.wollenlab.model.MessageRole
import com.pedra.wollenlab.presentation.viewmodel.ChatViewModel
import com.pedra.wollenlab.presentation.viewmodel.ConversationsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    conversationsViewModel: ConversationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val conversations by conversationsViewModel.conversations.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val errorPrefix = stringResource(R.string.chat_error_prefix)
    
    // Mostrar error si existe
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = "$errorPrefix$error",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationsDrawer(
                conversations = conversations,
                onConversationClick = { conversationId ->
                    viewModel.loadConversation(conversationId)
                    coroutineScope.launch { drawerState.close() }
                },
                onNewConversationClick = {
                    viewModel.createNewConversation()
                    coroutineScope.launch { drawerState.close() }
                },
                onDeleteConversation = { conversationId ->
                    conversationsViewModel.deleteConversation(conversationId)
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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
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
                        items(uiState.messages) { message ->
                            MessageBubble(message = message)
                        }

                        if (uiState.isLoading) {
                            item {
                                LoadingIndicator()
                            }
                        }
                    }

                    // Mensaje de bienvenida (solo cuando no hay mensajes y no está cargando)
                    if (uiState.messages.isEmpty() && !uiState.isLoading) {
                        WelcomeMessage(
                            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
                        )
                    }
                }

                // Input y botón de envío
                ChatInput(
                    text = uiState.inputText,
                    onTextChange = viewModel::updateInputText,
                    onSendClick = { viewModel.sendMessage(uiState.inputText) },
                    enabled = uiState.isInputEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: com.pedra.wollenlab.model.Message) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = if (isUser) TextAlign.End else TextAlign.Start
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 120.dp),
                placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
                enabled = enabled,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            IconButton(
                onClick = onSendClick,
                enabled = enabled && text.isNotBlank(),
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        if (enabled && text.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(R.string.chat_send),
                    tint = if (enabled && text.isNotBlank()) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomeMessage(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.chat_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

