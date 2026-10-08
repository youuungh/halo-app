package com.ninezero.halo.core.util

import com.ninezero.halo.BuildConfig

object Constants {
    const val BASE_URL = BuildConfig.HUB_BASE_URL
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // WebSocket
    const val WS_BASE_URL = BuildConfig.HUB_WS_BASE_URL

    /** Hub 카드 등록 페이지 URL */
    fun billingAuthUrl(customerKey: String): String =  // 결제·결제수단·멤버십 시트 공용
        "${BASE_URL}static/billing_auth.html" +
            "?clientKey=${BuildConfig.TOSS_CLIENT_KEY}&customerKey=$customerKey"

    // 페이지네이션
    const val DEFAULT_PAGE_SIZE = 20
    const val INITIAL_PAGE = 1
    const val MAX_PAGE_LIMIT = 100  // 서버가 한 번에 주는 최대 개수

    // 로컬 저장소
    const val DATABASE_NAME = "halo_database"

    object Commerce {
        const val FREE_SHIPPING_THRESHOLD = 50_000
        const val DEFAULT_SHIPPING_FEE = 3_000L
        const val MAX_CART_QUANTITY = 99
        const val REVIEW_WRITE_DEADLINE_DAYS = 30
    }

    // 비밀번호·사용자명 규칙
    object Auth {
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 20
        const val MIN_USERNAME_LENGTH = 5

        // 회원가입·비밀번호 재설정 공통 형식
        const val VERIFICATION_CODE_LENGTH = 6
    }

    // 본문 길이 상한
    object Content {
        const val MAX_POST_CONTENT_LENGTH = 2000
        const val MAX_COMMENT_CONTENT_LENGTH = 500
        const val MAX_MESSAGE_LENGTH = 1000
    }
}
