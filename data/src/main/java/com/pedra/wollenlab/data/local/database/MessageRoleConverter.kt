package com.pedra.wollenlab.data.local.database

import androidx.room.TypeConverter
import com.pedra.wollenlab.model.MessageRole

class MessageRoleConverter {
    @TypeConverter
    fun fromMessageRole(role: MessageRole): String {
        return role.name
    }

    @TypeConverter
    fun toMessageRole(role: String): MessageRole {
        return MessageRole.valueOf(role)
    }
}

