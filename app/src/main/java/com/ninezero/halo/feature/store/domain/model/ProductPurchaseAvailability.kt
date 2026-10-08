package com.ninezero.halo.feature.store.domain.model

private const val STATUS_SOLD_OUT = "SOLD_OUT"
private const val STATUS_DISCONTINUED = "DISCONTINUED"

fun Product.isPurchasable(): Boolean {
    return purchaseBlockedLabel() == null
}

fun Product.purchaseBlockedLabel(): String? {
    return when {
        isDiscontinued() -> "판매종료"
        isSoldOut() -> "품절"
        canAccess == false -> "멤버십 전용"
        else -> null
    }
}

fun Product.purchaseBlockedMessage(): String? {
    return when {
        isDiscontinued() -> "판매가 종료된 상품입니다"
        isSoldOut() -> "품절된 상품입니다"
        canAccess == false -> "$requiredTier 멤버십 전용 상품입니다"
        else -> null
    }
}

private fun Product.isDiscontinued(): Boolean {
    return !isActive || status.equals(STATUS_DISCONTINUED, ignoreCase = true)
}

private fun Product.isSoldOut(): Boolean {
    return stock <= 0 || status.equals(STATUS_SOLD_OUT, ignoreCase = true)
}
