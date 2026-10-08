package com.ninezero.halo.feature.point.di

import com.ninezero.halo.feature.point.data.remote.api.PointApi
import com.ninezero.halo.feature.point.data.repository.PointRepositoryImpl
import com.ninezero.halo.feature.point.domain.repository.PointRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PointModule {
    @Binds
    @Singleton
    abstract fun bindPointRepository(
        pointRepositoryImpl: PointRepositoryImpl
    ): PointRepository

    companion object {
        @Provides
        @Singleton
        fun providePointApi(retrofit: Retrofit): PointApi {
            return retrofit.create(PointApi::class.java)
        }
    }
}
