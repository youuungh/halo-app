package com.ninezero.halo.feature.chat.di

import com.ninezero.halo.feature.chat.data.remote.api.ChatApi
import com.ninezero.halo.feature.chat.data.repository.ChatEventRepositoryImpl
import com.ninezero.halo.feature.chat.data.repository.ChatMediaRepositoryImpl
import com.ninezero.halo.feature.chat.data.repository.ChatRepositoryImpl
import com.ninezero.halo.feature.chat.domain.repository.ChatEventRepository
import com.ninezero.halo.feature.chat.domain.repository.ChatMediaRepository
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindChatEventRepository(
        chatEventRepositoryImpl: ChatEventRepositoryImpl
    ): ChatEventRepository

    @Binds
    @Singleton
    abstract fun bindChatMediaRepository(
        chatMediaRepositoryImpl: ChatMediaRepositoryImpl
    ): ChatMediaRepository

    companion object {
        @Provides
        @Singleton
        fun provideChatApi(retrofit: Retrofit): ChatApi {
            return retrofit.create(ChatApi::class.java)
        }
    }
}
