package com.ninezero.halo.feature.store.data.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallNoData
import com.ninezero.halo.feature.store.data.remote.api.WishlistApi
import com.ninezero.halo.feature.store.data.remote.dto.toDomain
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.Wishlist
import com.ninezero.halo.feature.store.domain.repository.WishlistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepositoryImpl @Inject constructor(
    private val api: WishlistApi,
    private val storeEventBus: StoreEventBus
) : WishlistRepository {

    // 스낵바 소멸 시 커밋이라 화면을 나가도 끝까지 진행
    private val commitScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun getWishlist(cursor: String?, limit: Int): Result<CursorPage<Wishlist>> {
        return safeApiCall { api.getWishlist(cursor, limit) }
            .map { response -> CursorPage(response.items.map { it.toDomain() }, response.nextCursor) }
    }

    override suspend fun toggleWishlist(productId: Int): Result<Boolean> = commitScope.async {
        safeApiCall { api.toggleWishlist(productId) }
            .map { it.isWishlisted }
            .onSuccess { isWishlisted -> if (!isWishlisted) storeEventBus.emitWishlistRemoved(productId) }
    }.await()

    override suspend fun moveToCart(productId: Int): Result<Unit> = commitScope.async {
        // data가 null이라 safeApiCallNoData
        safeApiCallNoData { api.moveToCart(productId) }
    }.await()

    override suspend fun getWishlistStatus(productId: Int): Result<Boolean> {
        return safeApiCall { api.checkWishlistStatus(productId) }
            .map { it.isWishlisted }
    }
}
