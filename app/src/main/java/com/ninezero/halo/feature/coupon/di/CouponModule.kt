package com.ninezero.halo.feature.coupon.di

import com.ninezero.halo.feature.coupon.data.remote.api.CouponApi
import com.ninezero.halo.feature.coupon.data.repository.CouponRepositoryImpl
import com.ninezero.halo.feature.coupon.domain.repository.CouponRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CouponModule {
    @Binds
    @Singleton
    abstract fun bindCouponRepository(
        couponRepositoryImpl: CouponRepositoryImpl
    ): CouponRepository

    companion object {
        @Provides
        @Singleton
        fun provideCouponApi(retrofit: Retrofit): CouponApi {
            return retrofit.create(CouponApi::class.java)
        }
    }
}
