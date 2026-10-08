package com.ninezero.halo.core.ui.components.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

// #해시태그 매칭, 한글 포함
private val HASHTAG_REGEX = Regex("#[\\p{L}0-9_]+")

/** 해시태그만 강조한 AnnotatedString */
fun buildHashtagAnnotatedString(text: String, tagColor: Color): AnnotatedString = buildAnnotatedString {
    var lastIndex = 0
    for (match in HASHTAG_REGEX.findAll(text)) {
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }
        withStyle(SpanStyle(color = tagColor)) {
            append(match.value)
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}
