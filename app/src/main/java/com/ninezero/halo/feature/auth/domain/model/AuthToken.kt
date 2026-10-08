package com.ninezero.halo.feature.auth.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long?
)
