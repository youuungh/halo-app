package com.ninezero.halo.feature.store.di

import com.ninezero.halo.feature.store.data.local.CartSelectionStoreImpl
import com.ninezero.halo.feature.store.data.remote.api.BillingApi
import com.ninezero.halo.feature.store.data.remote.api.CartApi
import com.ninezero.halo.feature.store.data.remote.api.OrderApi
import com.ninezero.halo.feature.store.data.remote.api.PaymentApi
import com.ninezero.halo.feature.store.data.remote.api.ProductApi
import com.ninezero.halo.feature.store.data.remote.api.ReviewApi
import com.ninezero.halo.feature.store.data.remote.api.WishlistApi
import com.ninezero.halo.feature.store.data.repository.BillingRepositoryImpl
import com.ninezero.halo.feature.store.data.repository.CartRepositoryImpl
import com.ninezero.halo.feature.store.data.repository.OrderRepositoryImpl
import com.ninezero.halo.feature.store.data.repository.PaymentRepositoryImpl
import com.ninezero.halo.feature.store.data.repository.ProductRepositoryImpl
import com.ninezero.halo.feature.store.data.repository.ReviewRepositoryImpl
import com.ninezero.halo.feature.store.data.repository.WishlistRepositoryImpl
import com.ninezero.halo.feature.store.domain.repository.BillingRepository
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import com.ninezero.halo.feature.store.domain.repository.CartSelectionStore
import com.ninezero.halo.feature.store.domain.repository.OrderRepository
import com.ninezero.halo.feature.store.domain.repository.PaymentRepository
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import com.ninezero.halo.feature.store.domain.repository.ReviewRepository
import com.ninezero.halo.feature.store.domain.repository.WishlistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StoreModule {
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        reviewRepositoryImpl: ReviewRepositoryImpl
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindCartSelectionStore(
        cartSelectionStoreImpl: CartSelectionStoreImpl
    ): CartSelectionStore

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(
        wishlistRepositoryImpl: WishlistRepositoryImpl
    ): WishlistRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        billingRepositoryImpl: BillingRepositoryImpl
    ): BillingRepository

    companion object {
        @Provides
        @Singleton
        fun provideProductApi(retrofit: Retrofit): ProductApi {
            return retrofit.create(ProductApi::class.java)
        }

        @Provides
        @Singleton
        fun provideCartApi(retrofit: Retrofit): CartApi {
            return retrofit.create(CartApi::class.java)
        }

        @Provides
        @Singleton
        fun provideWishlistApi(retrofit: Retrofit): WishlistApi {
            return retrofit.create(WishlistApi::class.java)
        }

        @Provides
        @Singleton
        fun provideOrderApi(retrofit: Retrofit): OrderApi {
            return retrofit.create(OrderApi::class.java)
        }

        @Provides
        @Singleton
        fun providePaymentApi(retrofit: Retrofit): PaymentApi {
            return retrofit.create(PaymentApi::class.java)
        }

        @Provides
        @Singleton
        fun provideBillingApi(retrofit: Retrofit): BillingApi {
            return retrofit.create(BillingApi::class.java)
        }

        @Provides
        @Singleton
        fun provideReviewApi(retrofit: Retrofit): ReviewApi {
            return retrofit.create(ReviewApi::class.java)
        }
    }
}
