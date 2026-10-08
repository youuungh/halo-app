package com.ninezero.halo.feature.coupon.domain.repository

import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponListResult
import com.ninezero.halo.feature.coupon.domain.model.CreateCouponRequest
import com.ninezero.halo.feature.coupon.domain.model.CreatorCoupon
import com.ninezero.halo.feature.coupon.domain.model.CreatorCouponListResult
import com.ninezero.halo.feature.coupon.domain.model.UpdateCouponRequest
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus

interface CouponRepository {
    // 사용자용

    /** 내 쿠폰 목록 조회 */
    suspend fun getClaimedCoupons(
        status: UserCouponStatus? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<CouponListResult>

    /** 쿠폰 받기 */
    suspend fun claimCoupon(code: String): Result<Coupon>

    // 크리에이터용

    /** 내가 만든 쿠폰 목록 조회 */
    suspend fun getCreatedCoupons(
        page: Int = 1,
        limit: Int = 20
    ): Result<CreatorCouponListResult>

    /** 쿠폰 생성 */
    suspend fun createCoupon(request: CreateCouponRequest): Result<CreatorCoupon>

    /** 쿠폰 수정 */
    suspend fun updateCoupon(couponId: Int, request: UpdateCouponRequest): Result<CreatorCoupon>

    /** 쿠폰 삭제 */
    suspend fun deleteCoupon(couponId: Int): Result<Unit>
}
