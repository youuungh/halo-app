package com.ninezero.halo.feature.auth.domain.model

sealed interface SocialLoginResult {  // NeedsUsername이면 온보딩 후 complete
    data class Success(val user: User, val token: AuthToken) : SocialLoginResult
    data class NeedsUsername(val signupToken: String, val suggestedUsername: String) : SocialLoginResult
}
