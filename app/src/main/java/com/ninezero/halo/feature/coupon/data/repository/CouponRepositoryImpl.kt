package com.ninezero.halo.feature.coupon.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.core.util.toUtcIsoTimestamp
import com.ninezero.halo.feature.coupon.data.remote.api.CouponApi
import com.ninezero.halo.feature.coupon.data.remote.dto.ClaimCouponRequestDto
import com.ninezero.halo.feature.coupon.data.remote.dto.CreateCouponRequestDto
import com.ninezero.halo.feature.coupon.data.remote.dto.UpdateCouponRequestDto
import com.ninezero.halo.feature.coupon.data.remote.dto.toDomain
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponListResult
import com.ninezero.halo.feature.coupon.domain.model.CreateCouponRequest
import com.ninezero.halo.feature.coupon.domain.model.CreatorCoupon
import com.ninezero.halo.feature.coupon.domain.model.CreatorCouponListResult
import com.ninezero.halo.feature.coupon.domain.model.UpdateCouponRequest
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.coupon.domain.repository.CouponRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepositoryImpl @Inject constructor(
    private val api: CouponApi
) : CouponRepository {

    // 사용자용

    override suspend fun getClaimedCoupons(
        status: UserCouponStatus?,
        page: Int,
        limit: Int
    ): Result<CouponListResult> {
        return safeApiCall {
            api.getClaimedCoupons(status?.name, page, limit)
        }.map { it.toDomain() }
    }

    override suspend fun claimCoupon(code: String): Result<Coupon> {
        return safeApiCall {
            api.claimCoupon(ClaimCouponRequestDto(code))
        }.map { it.toDomain() }
    }

    // 크리에이터용

    override suspend fun getCreatedCoupons(
        page: Int,
        limit: Int
    ): Result<CreatorCouponListResult> {
        return safeApiCall {
            api.getCreatedCoupons(page, limit)
        }.map { it.toDomain() }
    }

    override suspend fun createCoupon(request: CreateCouponRequest): Result<CreatorCoupon> {
        return safeApiCall {
            api.createCoupon(
                CreateCouponRequestDto(
                    code = request.code,
                    name = request.name,
                    description = request.description,
                    type = request.type.name,
                    discountTarget = request.discountTarget.name,
                    discountValue = request.discountValue,
                    minOrderAmount = request.minOrderAmount,
                    maxDiscountAmount = request.maxDiscountAmount,
                    totalQuantity = request.totalQuantity,
                    maxUseCount = request.maxUseCount,
                    targetIds = request.targetIds,
                    startDate = request.startDate.toUtcIsoTimestamp(),
                    endDate = request.endDate.toUtcIsoTimestamp()
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun updateCoupon(couponId: Int, request: UpdateCouponRequest): Result<CreatorCoupon> {
        return safeApiCall {
            api.updateCoupon(
                couponId,
                UpdateCouponRequestDto(
                    name = request.name,
                    description = request.description,
                    minOrderAmount = request.minOrderAmount,
                    maxDiscountAmount = request.maxDiscountAmount,
                    totalQuantity = request.totalQuantity,
                    maxUseCount = request.maxUseCount,
                    status = request.status?.name,
                    startDate = request.startDate?.toUtcIsoTimestamp(),
                    endDate = request.endDate?.toUtcIsoTimestamp()
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun deleteCoupon(couponId: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deleteCoupon(couponId)
        }
    }
}
