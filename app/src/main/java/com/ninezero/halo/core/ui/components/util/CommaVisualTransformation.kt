package com.ninezero.halo.core.ui.components.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

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
                var orig = 0
                var trans = 0
                while (trans < offset && orig < original.length) {
                    orig++
                    trans++
                    if (orig < original.length) {
                        val remaining = original.length - 1 - orig
                        if (remaining > 0 && remaining % 3 == 0) trans++
                    }
                }
                return orig.coerceAtMost(original.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
