package com.ninezero.halo.feature.explore.domain.model

data class SearchCreatorsResult(
    val creators: List<Creator>,
    val hasMore: Boolean
)
