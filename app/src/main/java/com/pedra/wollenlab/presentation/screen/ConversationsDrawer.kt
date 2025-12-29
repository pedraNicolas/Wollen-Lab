package com.pedra.wollenlab.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pedra.wollenlab.R
import com.pedra.wollenlab.domain.model.Conversation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsDrawer(
    conversations: List<Conversation>,
    onConversationClick: (String) -> Unit,
    onNewConversationClick: () -> Unit,
    onDeleteConversation: (String) -> Unit,
    darkTheme: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Botón para nueva conversación
        Button(
            onClick = onNewConversationClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.btn_new_chat_description),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_new_chat))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.chat_history),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Divider()

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de conversaciones
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.chat_no_conversations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation.id) },
                        onDelete = { onDeleteConversation(conversation.id) },
                        darkTheme = darkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    darkTheme: Boolean = false
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateText = dateFormat.format(Date(conversation.updatedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = conversation.title ?: stringResource(R.string.chat_conversation_no_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.chat_delete),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

