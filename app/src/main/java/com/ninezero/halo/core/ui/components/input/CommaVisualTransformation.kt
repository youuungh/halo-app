package com.ninezero.halo.core.ui.components.input

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** 숫자 입력칸에 세 자리마다 쉼표 표시 */
object CommaVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formatted = buildString {
            original.forEachIndexed { index, c ->
                append(c)
                val remaining = original.length - 1 - index
                if (remaining > 0 && remaining % 3 == 0) append(',')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var commas = 0
                for (i in 0 until offset) {
                    val remaining = original.length - 1 - i
                    if (remaining > 0 && remaining % 3 == 0) commas++
                }
                return offset + commas
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalIndex = 0
                var transformedIndex = 0
                while (transformedIndex < offset && originalIndex < original.length) {
                    originalIndex++
                    transformedIndex++
                    if (originalIndex < original.length) {
                        val remaining = original.length - 1 - originalIndex
                        if (remaining > 0 && remaining % 3 == 0) transformedIndex++
                    }
                }
                return originalIndex.coerceAtMost(original.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
