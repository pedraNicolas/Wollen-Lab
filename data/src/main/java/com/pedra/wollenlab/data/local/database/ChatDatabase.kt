package com.pedra.wollenlab.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pedra.wollenlab.data.local.dao.ConversationDAO
import com.pedra.wollenlab.data.local.dao.MessageDAO
import com.pedra.wollenlab.data.local.entity.ConversationEntity
import com.pedra.wollenlab.data.local.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(MessageRoleConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDAO
    abstract fun messageDao(): MessageDAO
}

