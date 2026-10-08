package com.ninezero.halo.feature.explore.di

import com.ninezero.halo.feature.explore.data.remote.api.SearchApi
import com.ninezero.halo.feature.explore.data.repository.SearchRepositoryImpl
import com.ninezero.halo.feature.explore.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExploreModule {

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    companion object {
        @Provides
        @Singleton
        fun provideSearchApi(retrofit: Retrofit): SearchApi {
            return retrofit.create(SearchApi::class.java)
        }
    }
}
