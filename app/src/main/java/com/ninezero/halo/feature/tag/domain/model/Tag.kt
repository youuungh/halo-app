package com.ninezero.halo.feature.tag.domain.model

data class Tag(
    val id: Int,
    val name: String,
    val isSectionEnabled: Boolean = false,
    val itemCount: Int = 0
)

object TagTargetType {
    const val POST = "POST"
    const val PRODUCT = "PRODUCT"
}
