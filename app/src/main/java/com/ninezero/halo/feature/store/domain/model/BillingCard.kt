package com.ninezero.halo.feature.store.domain.model

data class BillingCard(
    val registered: Boolean,
    val cardCompany: String? = null,
    val cardNumberMasked: String? = null,
    val cardType: String? = null
)

fun isBillingAuthCanceled(code: String?): Boolean =
    code == null || code == "PAY_PROCESS_CANCELED" || code == "USER_CANCEL"

private val TRAILING_CODE_REGEX = Regex("\\s*\\([A-Za-z0-9_ .-]+\\)\\s*$")

/** 토스 실패 사유 */
fun billingAuthFailMessage(message: String?): String =
    message?.replace(TRAILING_CODE_REGEX, "")?.takeIf { it.isNotBlank() } ?: "카드 등록에 실패했습니다"
