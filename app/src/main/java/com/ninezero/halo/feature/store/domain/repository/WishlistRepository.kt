package com.ninezero.halo.feature.store.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.store.domain.model.Wishlist

interface WishlistRepository {
    /** 위시리스트 목록 조회 */
    suspend fun getWishlist(cursor: String?, limit: Int): Result<CursorPage<Wishlist>>

    /** 위시리스트 토글 */
    suspend fun toggleWishlist(productId: Int): Result<Boolean>

    /** 위시리스트에서 장바구니로 이동 */
    suspend fun moveToCart(productId: Int): Result<Unit>

    /** 위시리스트 상태 확인 */
    suspend fun getWishlistStatus(productId: Int): Result<Boolean>
}
