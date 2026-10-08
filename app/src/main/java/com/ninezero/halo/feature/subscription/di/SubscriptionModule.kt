package com.ninezero.halo.feature.subscription.di

import com.ninezero.halo.feature.subscription.data.remote.api.SubscriptionApi
import com.ninezero.halo.feature.subscription.data.repository.SubscriptionRepositoryImpl
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SubscriptionModule {
    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        subscriptionRepositoryImpl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    companion object {
        @Provides
        @Singleton
        fun provideSubscriptionApi(retrofit: Retrofit): SubscriptionApi {
            return retrofit.create(SubscriptionApi::class.java)
        }
    }
}
