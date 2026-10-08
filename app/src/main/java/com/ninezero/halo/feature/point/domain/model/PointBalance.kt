package com.ninezero.halo.feature.point.domain.model

data class PointBalance(
    val totalPoints: Int,
    val expiringPoints: Int = 0,
    val expiresAt: Long? = null
)
