package com.pedra.wollenlab.data.di

import android.content.Context
import androidx.room.Room
import com.pedra.wollenlab.data.local.database.ChatDatabase
import com.pedra.wollenlab.data.repository.ChatRepositoryImpl
import com.pedra.wollenlab.data.repository.ConversationRepositoryImpl
import com.pedra.wollenlab.domain.repository.ChatRepository
import com.pedra.wollenlab.domain.repository.ConversationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        conversationRepositoryImpl: ConversationRepositoryImpl
    ): ConversationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideConversationDao(database: ChatDatabase) = database.conversationDao()

    @Provides
    @Singleton
    fun provideMessageDao(database: ChatDatabase) = database.messageDao()
} 