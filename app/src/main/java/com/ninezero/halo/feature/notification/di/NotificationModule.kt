package com.ninezero.halo.feature.notification.di

import com.ninezero.halo.feature.notification.data.remote.api.NotificationApi
import com.ninezero.halo.feature.notification.data.repository.NotificationEventRepositoryImpl
import com.ninezero.halo.feature.notification.data.repository.NotificationRepositoryImpl
import com.ninezero.halo.feature.notification.domain.repository.NotificationEventRepository
import com.ninezero.halo.feature.notification.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationEventRepository(
        notificationEventRepositoryImpl: NotificationEventRepositoryImpl
    ): NotificationEventRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    companion object {
        @Provides
        @Singleton
        fun provideNotificationApi(retrofit: Retrofit): NotificationApi {
            return retrofit.create(NotificationApi::class.java)
        }
    }
}
