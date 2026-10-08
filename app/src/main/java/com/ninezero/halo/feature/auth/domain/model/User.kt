package com.ninezero.halo.feature.auth.domain.model

data class User(
    val id: Int,
    val username: String,
    val createdAt: Long,
    val hasPassword: Boolean = true,
    val socialProviders: List<String> = emptyList()
)
