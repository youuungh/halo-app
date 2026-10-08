package com.ninezero.halo.feature.address.di

import com.ninezero.halo.feature.address.data.remote.api.AddressApi
import com.ninezero.halo.feature.address.data.repository.AddressRepositoryImpl
import com.ninezero.halo.feature.address.domain.repository.AddressRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AddressModule {
    @Binds
    @Singleton
    abstract fun bindAddressRepository(
        addressRepositoryImpl: AddressRepositoryImpl
    ): AddressRepository

    companion object {
        @Provides
        @Singleton
        fun provideAddressApi(retrofit: Retrofit): AddressApi {
            return retrofit.create(AddressApi::class.java)
        }
    }
}
