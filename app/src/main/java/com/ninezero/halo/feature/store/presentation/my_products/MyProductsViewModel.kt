package com.ninezero.halo.feature.store.presentation.my_products

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.local.PreferencesManager
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.core.util.mergeFreshHead
import com.ninezero.halo.feature.store.domain.event.StoreEvent
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val PAGE_SIZE = 20

@HiltViewModel
class MyProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val preferencesManager: PreferencesManager,
    private val storeEventBus: StoreEventBus
) : ViewModel(), OrbitContainerHost<MyProductsState, MyProductsState, MyProductsSideEffect> {

    override val container: OrbitContainer<MyProductsState, MyProductsState, MyProductsSideEffect> =
        orbitContainer(MyProductsState())

    private val listGeneration = ListGeneration()

    init {
        load()
        observeGroupColors()
        observeStoreEvents()
    }

    // 하위 화면에서 바뀐 항목만 반영
    private fun observeStoreEvents() {
        storeEventBus.events
            .onEach { event ->
                when (event) {
                    is StoreEvent.ProductUpdated -> intent {
                        reduce { state.copy(products = state.products.map { if (it.id == event.product.id) event.product else it }) }
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: MyProductsIntent) {
        when (intent) {
            is MyProductsIntent.Load -> load()
            is MyProductsIntent.Refresh -> refresh()
            is MyProductsIntent.ScreenResumed -> refreshProductsQuietly()
            is MyProductsIntent.LoadMore -> loadMore()
            is MyProductsIntent.NavigateBack -> navigateBack()
            is MyProductsIntent.NavigateToCreateProduct -> navigateToCreateProduct()
            is MyProductsIntent.NavigateToEditProduct -> navigateToEditProduct(intent.product)
            is MyProductsIntent.NavigateToProductDetail -> navigateToProductDetail(intent.productId)
            is MyProductsIntent.ToggleProductStatus -> toggleProductStatus(intent.productId)
            is MyProductsIntent.ShowOptions -> showOptions(intent.product)
            is MyProductsIntent.DismissOptions -> dismissOptions()
            is MyProductsIntent.RequestSetDeal -> requestSetDeal(intent.product)
            is MyProductsIntent.DismissDealSetup -> dismissDealSetup()
            is MyProductsIntent.SetDeal -> setDeal(intent.productId, intent.dealPrice, intent.startAtMillis, intent.endAtMillis)
            is MyProductsIntent.ClearDeal -> clearDeal(intent.productId)
            is MyProductsIntent.ShowDeleteDialog -> showDeleteDialog(intent.product)
            is MyProductsIntent.DismissDeleteDialog -> dismissDeleteDialog()
            is MyProductsIntent.ConfirmDelete -> confirmDelete()
            is MyProductsIntent.DismissToast -> dismissToast()
            is MyProductsIntent.SnackbarDismissed -> dismissSnackbar()
            is MyProductsIntent.ToggleGroupCollapse -> toggleGroupCollapse(intent.tagId)
            is MyProductsIntent.ToggleTimeDealCollapse -> toggleTimeDealCollapse()
            is MyProductsIntent.UpdateGroupColor -> updateGroupColor(intent.tagId, intent.color)
        }
    }

    private fun load() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        reloadFirstPage(
            onSuccess = { products, nextCursor ->
                state.copy(
                    isLoading = false,
                    products = products,
                    nextCursor = nextCursor,
                    hasMore = nextCursor != null
                )
            },
            onFailure = { error ->
                state.copy(
                    isLoading = false,
                    error = error.message ?: "상품 목록을 불러올 수 없습니다"
                )
            }
        )
    }

    // 첫 페이지만 받아 앞에 합침
    private fun refreshProductsQuietly() = intent {
        if (state.isLoading || state.isRefreshing) return@intent
        productRepository.getMyProducts(null, PAGE_SIZE).onSuccess { result ->
            listGeneration.next()
            reduce {
                val merged = state.products.mergeFreshHead(result.items) { it.id }
                // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                val keepsTail = merged.size > result.items.size
                state.copy(
                    products = merged,
                    hasMore = if (keepsTail) state.hasMore else result.nextCursor != null,
                    nextCursor = if (keepsTail) state.nextCursor else result.nextCursor,
                    error = null
                )
            }
        }
    }

    private fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }

        reloadFirstPage(
            onSuccess = { products, nextCursor ->
                state.copy(
                    isRefreshing = false,
                    products = products,
                    nextCursor = nextCursor,
                    hasMore = nextCursor != null,
                    error = null
                )
            },
            onFailure = { error ->
                state.copy(
                    isRefreshing = false,
                    toastMessage = error.message ?: "새로고침 실패"
                )
            }
        )
    }

    private fun loadMore(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (state.isLoading || !state.hasMore) return@intent

        reduce { state.copy(isLoading = true) }

        val generation = listGeneration.current()
        productRepository.getMyProducts(state.nextCursor, PAGE_SIZE).onSuccess { result ->
            if (listGeneration.current() != generation) {
                reduce { state.copy(isLoading = false) }
                // 목록 수가 그대로라 트리거 대신 다시 요청
                if (state.hasMore) loadMore()
                return@intent
            }
            reduce {
                state.copy(
                    isLoading = false,
                    products = (state.products + result.items).distinctBy { it.id },
                    nextCursor = result.nextCursor,
                    hasMore = result.nextCursor != null
                )
            }
        }.onFailure { e ->
            reduce {
                state.copy(
                    isLoading = false,
                    snackbarState = SnackbarState(message = e.message ?: "더 불러오기 실패")
                )
            }
        }
    }

    private fun toggleProductStatus(productId: Int) = intent {
        val target = state.products.find { it.id == productId } ?: return@intent
        val newStatus = if (target.status == ProductStatus.DISCONTINUED) {
            ProductStatus.ACTIVE
        } else {
            ProductStatus.DISCONTINUED
        }

        reduce {
            state.copy(
                products = state.products.map {
                    if (it.id == productId) it.copy(status = newStatus) else it
                }
            )
        }

        productRepository.toggleProductStatus(productId).onSuccess {
            reduce {
                state.copy(
                    snackbarState = SnackbarState(
                        message = if (newStatus == ProductStatus.ACTIVE) "판매를 재개했습니다" else "판매를 종료했습니다"
                    )
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    products = state.products.map {
                        if (it.id == productId) target else it
                    },
                    snackbarState = SnackbarState(message = error.message ?: "상태 변경에 실패했습니다")
                )
            }
        }
    }

    private fun showOptions(product: Product) = intent {
        reduce { state.copy(optionSheetProduct = product) }
    }

    private fun dismissOptions() = intent {
        reduce { state.copy(optionSheetProduct = null) }
    }

    private fun requestSetDeal(product: Product) = intent {
        reduce { state.copy(optionSheetProduct = null, dealSetupProduct = product) }
    }

    private fun dismissDealSetup() = intent {
        reduce { state.copy(dealSetupProduct = null) }
    }

    private fun setDeal(productId: Int, dealPrice: String, startAtMillis: Long, endAtMillis: Long) = intent {
        reduce { state.copy(isSavingDeal = true) }

        productRepository.setProductDeal(productId, dealPrice, startAtMillis, endAtMillis).onSuccess { updated ->
            reduce {
                state.copy(
                    isSavingDeal = false,
                    dealSetupProduct = null,
                    // 응답에 sectionTag 없어 로컬 값 보존
                    products = state.products.map { if (it.id == productId) updated.copy(sectionTag = it.sectionTag) else it },
                    snackbarState = SnackbarState(message = "타임딜을 설정했습니다")
                )
            }
            postSideEffect(MyProductsSideEffect.ScrollToTop)
        }.onFailure { error ->
            reduce {
                state.copy(
                    isSavingDeal = false,
                    snackbarState = SnackbarState(message = error.message ?: "타임딜 설정에 실패했습니다")
                )
            }
        }
    }

    private fun clearDeal(productId: Int) = intent {
        productRepository.clearProductDeal(productId).onSuccess { updated ->
            reduce {
                state.copy(
                    // 응답에 sectionTag 없어 로컬 값 보존
                    products = state.products.map { if (it.id == productId) updated.copy(sectionTag = it.sectionTag) else it },
                    snackbarState = SnackbarState(message = "타임딜을 해제했습니다")
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(snackbarState = SnackbarState(message = error.message ?: "타임딜 해제에 실패했습니다"))
            }
        }
    }

    private fun showDeleteDialog(product: Product) = intent {
        reduce { state.copy(showDeleteDialog = true, deletingProduct = product) }
    }

    private fun dismissDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = false, deletingProduct = null) }
    }

    private fun confirmDelete() = intent {
        val product = state.deletingProduct ?: return@intent
        reduce { state.copy(showDeleteDialog = false) }

        productRepository.deleteProduct(product.id).onSuccess {
            val updatedProducts = state.products.filter { it.id != product.id }
            // 전체를 받았을 때만 섹션 색 정리
            if (!state.hasMore) {
                cleanupRemovedSectionColors(
                    oldProducts = state.products,
                    newProducts = updatedProducts
                )
            }
            reduce {
                state.copy(
                    deletingProduct = null,
                    products = updatedProducts,
                    snackbarState = SnackbarState(message = "상품이 삭제되었습니다")
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    deletingProduct = null,
                    snackbarState = SnackbarState(message = error.message ?: "삭제에 실패했습니다")
                )
            }
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(MyProductsSideEffect.NavigateBack)
    }

    private fun navigateToCreateProduct() = intent {
        postSideEffect(MyProductsSideEffect.NavigateToCreateProduct)
    }

    private fun navigateToEditProduct(product: Product) = intent {
        // 타임딜 활성 중에는 수정 차단
        if (product.isDealActive) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState(
                        message = "타임딜 진행 중에는 수정할 수 없습니다. 타임딜을 먼저 해제해주세요"
                    )
                )
            }
            return@intent
        }
        postSideEffect(MyProductsSideEffect.NavigateToEditProduct(product.id))
    }

    private fun navigateToProductDetail(productId: Int) = intent {
        postSideEffect(MyProductsSideEffect.NavigateToProductDetail(productId))
    }

    private fun dismissToast() = intent {
        reduce { state.copy(toastMessage = null) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun toggleGroupCollapse(tagId: Int) = intent {
        val newCollapsed = state.collapsedGroups.toMutableSet()
        if (tagId in newCollapsed) newCollapsed.remove(tagId) else newCollapsed.add(tagId)
        reduce { state.copy(collapsedGroups = newCollapsed) }
    }

    private fun toggleTimeDealCollapse() = intent {
        reduce { state.copy(isTimeDealCollapsed = !state.isTimeDealCollapsed) }
    }

    private fun updateGroupColor(tagId: Int, color: Color?) = intent {
        preferencesManager.setMyProductsGroupColor(
            tagId = tagId,
            colorArgb = color?.toArgb()
        )
        reduce {
            if (color != null) {
                state.copy(groupColors = state.groupColors + (tagId to color))
            } else {
                state.copy(groupColors = state.groupColors - tagId)
            }
        }
    }

    private fun observeGroupColors() {
        viewModelScope.launch {
            preferencesManager.observeMyProductsGroupColors().collect { storedColors ->
                intent {
                    reduce {
                        state.copy(
                            groupColors = storedColors.mapValues { Color(it.value) }
                        )
                    }
                }
            }
        }
    }

    private suspend fun cleanupRemovedSectionColors(
        oldProducts: List<Product>,
        newProducts: List<Product>
    ) {
        val previousSectionIds = oldProducts.mapNotNull { it.sectionTag?.id }.toSet()
        val currentSectionIds = newProducts.mapNotNull { it.sectionTag?.id }.toSet()
        val removedSectionIds = previousSectionIds - currentSectionIds
        preferencesManager.removeMyProductsGroupColors(removedSectionIds)
    }

    private suspend fun Syntax<MyProductsState, MyProductsSideEffect>.reloadFirstPage(
        onSuccess: MyProductsState.(products: List<Product>, nextCursor: String?) -> MyProductsState,
        onFailure: MyProductsState.(Throwable) -> MyProductsState
    ) {
        productRepository.getMyProducts(null, PAGE_SIZE).onSuccess { result ->
            listGeneration.next()
            reduce { state.onSuccess(result.items, result.nextCursor) }
        }.onFailure { error ->
            reduce { state.onFailure(error) }
        }
    }
}
