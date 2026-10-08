package com.ninezero.halo.feature.auth.data.remote.dto

import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.auth.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,

    val email: String = "",

    val username: String,

    val role: String = "USER",


    val createdAt: String,

    val hasPassword: Boolean = true,  // false면 간편로그인 전용

    val socialProviders: List<String> = emptyList()  // GOOGLE·KAKAO·NAVER
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        username = username,
        createdAt = createdAt.parseTimestamp(),
        hasPassword = hasPassword,
        socialProviders = socialProviders
    )
}
