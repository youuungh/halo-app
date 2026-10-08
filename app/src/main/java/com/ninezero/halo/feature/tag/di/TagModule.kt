package com.ninezero.halo.feature.tag.di

import com.ninezero.halo.feature.tag.data.remote.api.TagApi
import com.ninezero.halo.feature.tag.data.repository.TagRepositoryImpl
import com.ninezero.halo.feature.tag.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TagModule {

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        tagRepositoryImpl: TagRepositoryImpl
    ): TagRepository

    companion object {
        @Provides
        @Singleton
        fun provideTagApi(retrofit: Retrofit): TagApi {
            return retrofit.create(TagApi::class.java)
        }
    }
}
