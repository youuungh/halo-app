package com.ninezero.halo.feature.address.presentation.address_detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** 서버와 같은 하이픈, 02는 두 자리 지역번호 */
internal fun formatPhoneWithHyphen(digits: String): String {
    val onlyDigits = digits.filter { it.isDigit() }
    return if (onlyDigits.startsWith("02")) {
        when (onlyDigits.length) {
            in 0..2 -> onlyDigits
            in 3..5 -> "${onlyDigits.take(2)}-${onlyDigits.drop(2)}"
            in 6..9 -> "${onlyDigits.take(2)}-${onlyDigits.substring(2, onlyDigits.length - 4)}-${onlyDigits.takeLast(4)}"
            else -> "${onlyDigits.take(2)}-${onlyDigits.substring(2, 6)}-${onlyDigits.takeLast(4)}"
        }
    } else {
        when (onlyDigits.length) {
            in 0..3 -> onlyDigits
            in 4..7 -> "${onlyDigits.take(3)}-${onlyDigits.drop(3)}"
            in 8..10 -> "${onlyDigits.take(3)}-${onlyDigits.substring(3, onlyDigits.length - 4)}-${onlyDigits.takeLast(4)}"
            else -> "${onlyDigits.take(3)}-${onlyDigits.substring(3, 7)}-${onlyDigits.takeLast(4)}"
        }
    }
}

/** 숫자만 저장하고 보이는 것만 하이픈 */
internal object PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formatted = formatPhoneWithHyphen(original)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var digits = 0
                formatted.forEachIndexed { index, c ->
                    if (digits == offset) return index
                    if (c != '-') digits++
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset).count { it != '-' }.coerceAtMost(original.length)
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
