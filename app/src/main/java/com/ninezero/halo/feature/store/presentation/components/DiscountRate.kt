package com.ninezero.halo.feature.store.presentation.components

import java.math.BigDecimal
import java.math.RoundingMode

/** 기준가 대비 할인율, 서버와 같은 HALF_UP */
fun discountRatePercent(originalPrice: String, price: String): Int? {
    val original = originalPrice.toBigDecimalOrNull() ?: return null
    val sale = price.toBigDecimalOrNull() ?: return null
    if (original <= BigDecimal.ZERO || sale <= BigDecimal.ZERO || sale >= original) return null
    return ((original - sale) * BigDecimal(100)).divide(original, 0, RoundingMode.HALF_UP).toInt().takeIf { it > 0 }
}
