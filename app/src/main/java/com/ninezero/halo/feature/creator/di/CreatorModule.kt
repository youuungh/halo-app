package com.ninezero.halo.feature.creator.di

import com.ninezero.halo.feature.creator.data.remote.api.CreatorApi
import com.ninezero.halo.feature.creator.data.repository.CreatorRepositoryImpl
import com.ninezero.halo.feature.creator.domain.repository.CreatorRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CreatorModule {
    @Binds
    @Singleton
    abstract fun bindCreatorRepository(
        creatorRepositoryImpl: CreatorRepositoryImpl
    ): CreatorRepository

    companion object {
        @Provides
        @Singleton
        fun provideCreatorApi(retrofit: Retrofit): CreatorApi {
            return retrofit.create(CreatorApi::class.java)
        }
    }
}
