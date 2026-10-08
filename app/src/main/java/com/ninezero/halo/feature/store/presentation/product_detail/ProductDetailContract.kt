package com.ninezero.halo.feature.store.presentation.product_detail

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo

data class ProductDetailState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    val reviewSummary: ReviewSummary? = null,
    val recentReviews: List<Review> = emptyList(),
    val creatorProducts: List<Product> = emptyList(),
    val relatedProducts: List<Product> = emptyList(),
    val selectedDetailTab: Int = 0,
    val isInfoExpanded: Boolean = false,
    val isDetailExpanded: Boolean = false,
    val isImageViewerVisible: Boolean = false,
    val imageViewerUrls: List<String> = emptyList(),
    val imageViewerInitialIndex: Int = 0,
    val isWishlisted: Boolean = false,
    val showPurchaseSheet: Boolean = false,
    val purchaseQuantity: Int = 1,
    val isAddingToCart: Boolean = false,
    val bestCoupon: Coupon? = null,
    val showCouponDetailDialog: Boolean = false,
    val userTierInfo: UserTierInfo? = null,
    val cartItemCount: Int = 0,
    // 공유
    val showShareSheet: Boolean = false,
    val navigateRoomId: Int? = null
)

sealed interface ProductDetailIntent {
    // 데이터 로딩
    data object LoadProduct : ProductDetailIntent
    data object RefreshCartCount : ProductDetailIntent
    data object RefreshReviewPreview : ProductDetailIntent
    data object RefreshProduct : ProductDetailIntent
    data object ToggleInfoExpanded : ProductDetailIntent
    data object ToggleDetailExpanded : ProductDetailIntent
    data class SelectDetailTab(val index: Int) : ProductDetailIntent
    data object ToggleWishlist : ProductDetailIntent
    data object ShareProduct : ProductDetailIntent
    data object DismissShareSheet : ProductDetailIntent
    data class ShareCompleted(val sharedRoomIds: List<Int>, val recipientCount: Int) : ProductDetailIntent
    data class NavigateToChatRoom(val roomId: Int) : ProductDetailIntent

    // 이미지 뷰어
    data class ShowImageViewer(val urls: List<String>, val index: Int) : ProductDetailIntent
    data object DismissImageViewer : ProductDetailIntent

    // 구매 시트
    data object ShowPurchaseSheet : ProductDetailIntent
    data object DismissPurchaseSheet : ProductDetailIntent
    data class UpdateQuantity(val quantity: Int) : ProductDetailIntent
    data object AddToCart : ProductDetailIntent
    data object BuyNow : ProductDetailIntent

    // 쿠폰
    data object ShowCouponDetailDialog : ProductDetailIntent
    data object DismissCouponDetailDialog : ProductDetailIntent

    data object NavigateBack : ProductDetailIntent
    data object NavigateToCart : ProductDetailIntent
    data class NavigateToReviewList(val productId: Int) : ProductDetailIntent
    data class NavigateToProductDetail(val productId: Int) : ProductDetailIntent
    data object SnackbarDismissed : ProductDetailIntent
    data class ShowRestrictedContentSnackbar(val message: String) : ProductDetailIntent
}

sealed interface ProductDetailSideEffect {
    data object NavigateBack : ProductDetailSideEffect
    data object NavigateToCart : ProductDetailSideEffect
    data class NavigateToDirectCheckout(val productId: Int, val quantity: Int) : ProductDetailSideEffect
    data class NavigateToReviewList(val productId: Int) : ProductDetailSideEffect
    data class NavigateToProductDetail(val productId: Int) : ProductDetailSideEffect
    data class ShowCenterToast(val message: String) : ProductDetailSideEffect
    data class NavigateToChatRoom(val roomId: Int) : ProductDetailSideEffect
}
