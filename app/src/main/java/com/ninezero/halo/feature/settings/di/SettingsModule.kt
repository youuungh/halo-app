package com.ninezero.halo.feature.settings.di

import com.ninezero.halo.feature.settings.data.repository.OpenSourceLicenseRepositoryImpl
import com.ninezero.halo.feature.settings.domain.repository.OpenSourceLicenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    abstract fun bindOpenSourceLicenseRepository(
        impl: OpenSourceLicenseRepositoryImpl
    ): OpenSourceLicenseRepository
}
