package com.ninezero.halo.feature.feed.presentation

enum class CommentSortType(val apiValue: String, val displayName: String) {
    POPULAR("POPULAR", "인기순"),
    LATEST("LATEST", "최신순")
}
