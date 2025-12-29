package com.pedra.wollenlab.di

import android.content.Context
import com.pedra.wollenlab.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    @Named("GeminiApiKey")
    fun provideGeminiApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }
} 