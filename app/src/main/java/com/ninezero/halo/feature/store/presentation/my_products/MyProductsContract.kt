package com.ninezero.halo.feature.store.presentation.my_products

import androidx.compose.ui.graphics.Color
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.Product

data class MyProductsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    // 상품 목록
    val products: List<Product> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = true,
    val error: String? = null,
    // 삭제 다이얼로그
    val showDeleteDialog: Boolean = false,
    val deletingProduct: Product? = null,
    // 상품 옵션 시트
    val optionSheetProduct: Product? = null,
    // 타임딜 설정 시트
    val dealSetupProduct: Product? = null,
    val isSavingDeal: Boolean = false,
    // 피드백
    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null,
    // 그룹 접기·색상
    val collapsedGroups: Set<Int> = emptySet(),
    val isTimeDealCollapsed: Boolean = false,
    val groupColors: Map<Int, Color> = emptyMap()
)

sealed interface MyProductsIntent {
    data object Load : MyProductsIntent
    data object Refresh : MyProductsIntent
    data object ScreenResumed : MyProductsIntent
    data object LoadMore : MyProductsIntent

    data object NavigateBack : MyProductsIntent
    data object NavigateToCreateProduct : MyProductsIntent
    data class NavigateToEditProduct(val product: Product) : MyProductsIntent
    data class NavigateToProductDetail(val productId: Int) : MyProductsIntent

    // 판매 상태·옵션
    data class ToggleProductStatus(val productId: Int) : MyProductsIntent
    data class ShowOptions(val product: Product) : MyProductsIntent
    data object DismissOptions : MyProductsIntent

    // 타임딜
    data class RequestSetDeal(val product: Product) : MyProductsIntent
    data object DismissDealSetup : MyProductsIntent
    data class SetDeal(
        val productId: Int,
        val dealPrice: String,
        val startAtMillis: Long,
        val endAtMillis: Long
    ) : MyProductsIntent
    data class ClearDeal(val productId: Int) : MyProductsIntent

    // 삭제
    data class ShowDeleteDialog(val product: Product) : MyProductsIntent
    data object DismissDeleteDialog : MyProductsIntent
    data object ConfirmDelete : MyProductsIntent

    // 피드백
    data object DismissToast : MyProductsIntent
    data object SnackbarDismissed : MyProductsIntent

    // 그룹 접기·색상
    data class ToggleGroupCollapse(val tagId: Int) : MyProductsIntent
    data object ToggleTimeDealCollapse : MyProductsIntent
    data class UpdateGroupColor(val tagId: Int, val color: Color?) : MyProductsIntent
}

sealed interface MyProductsSideEffect {
    data object NavigateBack : MyProductsSideEffect
    data object NavigateToCreateProduct : MyProductsSideEffect
    data class NavigateToEditProduct(val productId: Int) : MyProductsSideEffect
    data class NavigateToProductDetail(val productId: Int) : MyProductsSideEffect
    data object ScrollToTop : MyProductsSideEffect
}
