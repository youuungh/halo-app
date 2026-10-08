package com.ninezero.halo.feature.store.presentation.components

import androidx.annotation.DrawableRes
import com.ninezero.halo.feature.store.domain.model.BillingCard

/** 토스 발급사 코드별 카드사명 */
private val CARD_ISSUER_CODES = mapOf(
    "3K" to "기업비씨", "46" to "광주", "71" to "롯데", "30" to "산업",
    "31" to "BC", "51" to "삼성", "38" to "새마을", "41" to "신한",
    "62" to "신협", "36" to "씨티", "33" to "우리", "W1" to "우리",
    "37" to "우체국", "39" to "저축", "35" to "전북", "42" to "제주",
    "15" to "카카오뱅크", "3A" to "케이뱅크", "24" to "토스뱅크",
    "21" to "하나", "61" to "현대", "11" to "국민", "91" to "농협", "34" to "수협"
)

fun cardIssuerName(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return null
    return CARD_ISSUER_CODES[value.uppercase()]?.let { "${it}카드" } ?: value
}

/** 저장카드 표시 라벨 */
fun cardLabel(card: BillingCard?): String {
    val name = cardIssuerName(card?.cardCompany) ?: "저장카드"
    val tail = card?.cardNumberMasked?.trim()?.takeIf { it.isNotBlank() }?.takeLast(4)
    return if (tail != null) "$name ($tail)" else name
}

@DrawableRes
fun cardLogoRes(raw: String?): Int? {
    val key = raw?.trim()?.uppercase()?.takeIf { it.isNotBlank() } ?: return null
    return CARD_LOGOS[key]
}

// 카드사 코드별 로고, 리소스 추가 때 채움
private val CARD_LOGOS: Map<String, Int> = emptyMap()
