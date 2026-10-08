package com.ninezero.halo.feature.auth.domain.model

data class AuthSession(
    val user: User,
    val token: AuthToken
)
