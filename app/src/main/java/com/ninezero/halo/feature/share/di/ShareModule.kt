package com.ninezero.halo.feature.share.di

import com.ninezero.halo.feature.share.data.remote.api.ShareApi
import com.ninezero.halo.feature.share.data.repository.ShareRepositoryImpl
import com.ninezero.halo.feature.share.domain.repository.ShareRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShareModule {

    @Binds
    @Singleton
    abstract fun bindShareRepository(
        shareRepositoryImpl: ShareRepositoryImpl
    ): ShareRepository

    companion object {
        @Provides
        @Singleton
        fun provideShareApi(retrofit: Retrofit): ShareApi {
            return retrofit.create(ShareApi::class.java)
        }
    }
}
