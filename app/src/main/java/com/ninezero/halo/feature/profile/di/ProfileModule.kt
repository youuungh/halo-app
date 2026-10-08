package com.ninezero.halo.feature.profile.di

import com.ninezero.halo.feature.profile.data.remote.api.FollowApi
import com.ninezero.halo.feature.profile.data.remote.api.ProfileApi
import com.ninezero.halo.feature.profile.data.repository.FollowRepositoryImpl
import com.ninezero.halo.feature.profile.data.repository.ProfileRepositoryImpl
import com.ninezero.halo.feature.profile.domain.repository.FollowRepository
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindFollowRepository(
        followRepositoryImpl: FollowRepositoryImpl
    ): FollowRepository

    companion object {
        @Provides
        @Singleton
        fun provideProfileApi(retrofit: Retrofit): ProfileApi {
            return retrofit.create(ProfileApi::class.java)
        }

        @Provides
        @Singleton
        fun provideFollowApi(retrofit: Retrofit): FollowApi {
            return retrofit.create(FollowApi::class.java)
        }
    }
}
