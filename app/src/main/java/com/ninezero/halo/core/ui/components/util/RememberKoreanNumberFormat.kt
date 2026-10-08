package com.ninezero.halo.core.ui.components.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Locale

/** 한국어 숫자 포맷 state */
@Composable
fun rememberKoreanNumberFormat(): NumberFormat {
    return remember { NumberFormat.getNumberInstance(Locale.KOREA) }
}
