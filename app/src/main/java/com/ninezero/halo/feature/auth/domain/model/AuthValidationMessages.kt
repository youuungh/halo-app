package com.ninezero.halo.feature.auth.domain.model

import com.ninezero.halo.core.util.Constants

object AuthValidationMessages {
    const val EMAIL_REQUIRED = "이메일을 입력해주세요"
    const val EMAIL_INVALID = "올바른 이메일 형식이 아닙니다"
    const val USERNAME_REQUIRED = "사용자명을 입력해주세요"
    const val USERNAME_TOO_SHORT = "사용자명은 ${Constants.Auth.MIN_USERNAME_LENGTH}자 이상이어야 합니다"
    const val PASSWORD_REQUIRED = "비밀번호를 입력해주세요"
    const val PASSWORD_INVALID =
        "비밀번호는 ${Constants.Auth.MIN_PASSWORD_LENGTH}~${Constants.Auth.MAX_PASSWORD_LENGTH}자, 영문/숫자/특수문자를 포함해야 합니다"
    const val PASSWORD_INVALID_SHORT =
        "${Constants.Auth.MIN_PASSWORD_LENGTH}~${Constants.Auth.MAX_PASSWORD_LENGTH}자, 영문/숫자/특수문자 포함"
    const val PASSWORD_CONFIRM_REQUIRED = "비밀번호를 다시 입력해주세요"
    const val PASSWORD_MISMATCH = "비밀번호가 일치하지 않습니다"
    const val CURRENT_PASSWORD_REQUIRED = "현재 비밀번호를 입력해주세요"
    const val PASSWORD_SAME_AS_CURRENT = "현재 비밀번호와 다른 비밀번호를 입력해주세요"
    const val CODE_REQUIRED = "인증 코드를 입력해주세요"
    const val CODE_LENGTH = "인증 코드 6자리를 입력해주세요"
}
