package com.ninezero.halo.feature.auth.di

import com.ninezero.halo.core.fcm.FcmRegistrationRepository
import com.ninezero.halo.core.network.auth.TokenProvider
import com.ninezero.halo.feature.auth.data.local.TokenManager
import com.ninezero.halo.feature.auth.data.remote.api.AuthApi
import com.ninezero.halo.feature.auth.data.repository.AuthRepositoryImpl
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.DeviceRepository
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFcmRegistrationRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): FcmRegistrationRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindTokenProvider(
        tokenManager: TokenManager
    ): TokenProvider

    @Binds
    @Singleton
    abstract fun bindTokenStore(
        tokenManager: TokenManager
    ): TokenStore

    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi {
            return retrofit.create(AuthApi::class.java)
        }
    }
}
