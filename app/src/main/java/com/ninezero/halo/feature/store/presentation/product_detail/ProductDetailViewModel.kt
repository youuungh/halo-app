package com.ninezero.halo.feature.store.presentation.product_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.coupon.domain.repository.CouponRepository
import com.ninezero.halo.feature.profile.domain.usecase.GetCachedMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.calcSingleCouponDiscount
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import com.ninezero.halo.feature.store.domain.repository.WishlistRepository
import com.ninezero.halo.feature.store.domain.usecase.GetProductReviewsUseCase
import com.ninezero.halo.feature.store.domain.usecase.GetProductUseCase
import com.ninezero.halo.feature.store.domain.usecase.GetReviewSummaryUseCase
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
    private val getReviewSummaryUseCase: GetReviewSummaryUseCase,
    private val getProductReviewsUseCase: GetProductReviewsUseCase,
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
    private val couponRepository: CouponRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val getCachedMyProfileUseCase: GetCachedMyProfileUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), OrbitContainerHost<ProductDetailState, ProductDetailState, ProductDetailSideEffect> {

    private val productId: Int = savedStateHandle.get<Int>("productId") ?: 0

    // 중복 요청 방지 플래그
    private var isWishlistInProgress = false

    override val container: OrbitContainer<ProductDetailState, ProductDetailState, ProductDetailSideEffect> =
        orbitContainer(ProductDetailState())

    init {
        observeCartCount()
        loadProduct()
        refreshCartCount()
    }

    fun onIntent(intent: ProductDetailIntent) {
        when (intent) {
            is ProductDetailIntent.LoadProduct -> loadProduct()
            is ProductDetailIntent.RefreshCartCount -> refreshCartCount()
            is ProductDetailIntent.RefreshReviewPreview -> refreshReviewPreview()
            is ProductDetailIntent.RefreshProduct -> refreshProduct()
            is ProductDetailIntent.ToggleInfoExpanded -> toggleInfoExpanded()
            is ProductDetailIntent.ToggleDetailExpanded -> toggleDetailExpanded()
            is ProductDetailIntent.SelectDetailTab -> selectDetailTab(intent.index)
            is ProductDetailIntent.ToggleWishlist -> toggleWishlist()
            is ProductDetailIntent.ShareProduct -> shareProduct()
            is ProductDetailIntent.DismissShareSheet -> dismissShareSheet()
            is ProductDetailIntent.ShareCompleted -> shareCompleted(
                intent.sharedRoomIds,
                intent.recipientCount
            )

            is ProductDetailIntent.NavigateToChatRoom -> navigateToChatRoom(intent.roomId)
            is ProductDetailIntent.ShowImageViewer -> showImageViewer(intent.urls, intent.index)
            is ProductDetailIntent.DismissImageViewer -> dismissImageViewer()
            is ProductDetailIntent.ShowPurchaseSheet -> showPurchaseSheet()
            is ProductDetailIntent.DismissPurchaseSheet -> dismissPurchaseSheet()
            is ProductDetailIntent.UpdateQuantity -> updateQuantity(intent.quantity)
            is ProductDetailIntent.AddToCart -> addToCart()
            is ProductDetailIntent.BuyNow -> buyNow()
            is ProductDetailIntent.ShowCouponDetailDialog -> showCouponDetailDialog()
            is ProductDetailIntent.DismissCouponDetailDialog -> dismissCouponDetailDialog()
            is ProductDetailIntent.NavigateBack -> navigateBack()
            is ProductDetailIntent.NavigateToCart -> navigateToCart()
            is ProductDetailIntent.NavigateToReviewList -> navigateToReviewList(intent.productId)
            is ProductDetailIntent.NavigateToProductDetail -> navigateToProductDetail(intent.productId)
            is ProductDetailIntent.SnackbarDismissed -> dismissSnackbar()
            is ProductDetailIntent.ShowRestrictedContentSnackbar -> showRestrictedSnackbar(intent.message)
        }
    }

    private fun loadProduct() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        val productResult = getProductUseCase(productId)
        val product = productResult.getOrNull()

        if (product == null) {
            reduce {
                state.copy(
                    isLoading = false,
                    error = productResult.exceptionOrNull()?.message ?: "연결에 실패했습니다",
                    snackbarState = SnackbarState(
                        message = productResult.exceptionOrNull()?.message ?: "연결에 실패했습니다",
                        actionLabel = "재시도",
                        // 재시도·dismiss 전까지 유지
                        persistent = true
                    )
                )
            }
            return@intent
        }

        val creatorId = product.creator.id
        viewModelScope.launch {
            val reviewSummaryDeferred = async { getReviewSummaryUseCase(productId) }
            val recentReviewsDeferred =
                async { getProductReviewsUseCase(productId, limit = 5) }
            val wishlistStatusDeferred = async { wishlistRepository.getWishlistStatus(productId) }
            val couponsDeferred =
                async { product.price.toIntOrNull()?.let { loadBestCoupon(creatorId, it) } }
            val tierDeferred = async { loadUserTierInfo(creatorId) }

            val summaryResult = reviewSummaryDeferred.await()
            val reviewsResult = recentReviewsDeferred.await()
            val wishlistStatus = wishlistStatusDeferred.await()
            val bestCoupon = couponsDeferred.await()
            val userTierInfo = tierDeferred.await()

            intent {
                reduce {
                    state.copy(
                        product = product,
                        isLoading = false,
                        error = null,
                        reviewSummary = summaryResult.getOrNull(),
                        recentReviews = reviewsResult.getOrNull()?.items ?: emptyList(),
                        isWishlisted = wishlistStatus.getOrDefault(false),
                        bestCoupon = bestCoupon,
                        userTierInfo = userTierInfo
                    )
                }
            }

            // 보조 데이터 별도 로드
            loadExtraData(creatorId)
        }
    }

    private fun loadExtraData(creatorId: Int) {
        viewModelScope.launch {
            val creatorProductsDeferred = async { loadCreatorProducts(creatorId) }
            val relatedProductsDeferred = async { loadRelatedProducts() }

            val creatorProducts = creatorProductsDeferred.await()
            val relatedProducts = relatedProductsDeferred.await()

            intent {
                reduce {
                    state.copy(
                        creatorProducts = creatorProducts,
                        relatedProducts = relatedProducts
                    )
                }
            }
        }
    }

    private suspend fun loadUserTierInfo(creatorId: Int): UserTierInfo? {
        return subscriptionRepository.getCreatorTier(creatorId).getOrNull()
            ?.takeIf { it.isSubscribed }
    }

    private suspend fun loadBestCoupon(creatorId: Int, productPrice: Int): Coupon? {
        return couponRepository.getClaimedCoupons(
            status = UserCouponStatus.AVAILABLE,
            page = 1,
            limit = 100
        )
            .getOrNull()
            ?.coupons
            ?.filter { coupon ->
                coupon.creatorId == creatorId &&
                        coupon.type != CouponType.FREE_SHIPPING &&
                        (coupon.minOrderAmount?.toIntOrNull() ?: 0) <= productPrice
            }
            ?.maxByOrNull { calcSingleCouponDiscount(it, productPrice.toLong()) }
            ?.takeIf { calcSingleCouponDiscount(it, productPrice.toLong()) > 0 }
    }

    private suspend fun loadCreatorProducts(creatorId: Int): List<Product> {
        return productRepository.getCreatorProducts(
            creatorId,
            excludeProductId = productId,
            limit = 10
        )
            .getOrDefault(emptyList())
    }

    private suspend fun loadRelatedProducts(): List<Product> {
        return productRepository.getRelatedProducts(productId, limit = 10)
            .getOrDefault(emptyList())
    }

    private fun navigateBack() = intent {
        postSideEffect(ProductDetailSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun toggleInfoExpanded() = intent {
        reduce { state.copy(isInfoExpanded = !state.isInfoExpanded) }
    }

    private fun toggleDetailExpanded() = intent {
        reduce { state.copy(isDetailExpanded = !state.isDetailExpanded) }
    }

    private fun selectDetailTab(index: Int) = intent {
        reduce { state.copy(selectedDetailTab = index) }
    }

    private fun navigateToCart() = intent {
        postSideEffect(ProductDetailSideEffect.NavigateToCart)
    }

    private fun shareProduct() = intent {
        reduce { state.copy(showShareSheet = true) }
    }

    private fun dismissShareSheet() = intent {
        reduce { state.copy(showShareSheet = false) }
    }

    private fun shareCompleted(sharedRoomIds: List<Int>, recipientCount: Int) = intent {
        reduce { state.copy(showShareSheet = false) }
        if (recipientCount == 1) {
            val roomId = sharedRoomIds.first()
            reduce {
                state.copy(
                    snackbarState = SnackbarState("메시지를 보냈습니다", actionLabel = "채팅 열기"),
                    navigateRoomId = roomId
                )
            }
        } else {
            reduce {
                state.copy(
                    snackbarState = SnackbarState("${recipientCount}명에게 메시지를 보냈습니다")
                )
            }
        }
    }

    private fun navigateToChatRoom(roomId: Int) = intent {
        reduce { state.copy(navigateRoomId = null) }
        postSideEffect(ProductDetailSideEffect.NavigateToChatRoom(roomId))
    }

    private fun navigateToReviewList(productId: Int) = intent {
        postSideEffect(ProductDetailSideEffect.NavigateToReviewList(productId))
    }

    private fun navigateToProductDetail(productId: Int) = intent {
        postSideEffect(ProductDetailSideEffect.NavigateToProductDetail(productId))
    }

    private fun showRestrictedSnackbar(message: String) = intent {
        reduce { state.copy(snackbarState = SnackbarState(message)) }
    }

    private fun showImageViewer(urls: List<String>, index: Int) = intent {
        reduce {
            state.copy(
                isImageViewerVisible = true,
                imageViewerUrls = urls,
                imageViewerInitialIndex = index
            )
        }
    }

    private fun dismissImageViewer() = intent {
        reduce { state.copy(isImageViewerVisible = false) }
    }

    // 위시리스트
    private fun toggleWishlist() = intent {
        if (isWishlistInProgress) return@intent
        isWishlistInProgress = true

        val product = state.product ?: run { isWishlistInProgress = false; return@intent }
        val prevIsWishlisted = state.isWishlisted
        val prevLikeCount = product.likeCount

        reduce {
            state.copy(
                isWishlisted = !prevIsWishlisted,
                product = product.copy(
                    likeCount = if (prevIsWishlisted) prevLikeCount - 1 else prevLikeCount + 1
                )
            )
        }

        wishlistRepository.toggleWishlist(productId)
            .onSuccess { isWishlisted ->
                // 서버 토글 결과로 확정
                reduce {
                    state.copy(
                        isWishlisted = isWishlisted,
                        product = state.product?.copy(likeCount = (prevLikeCount + if (isWishlisted) 1 else -1).coerceAtLeast(0))
                    )
                }
                val message = if (isWishlisted) "위시리스트에 추가됨" else "위시리스트에서 제거됨"
                postSideEffect(ProductDetailSideEffect.ShowCenterToast(message))
            }
            .onFailure {
                reduce {
                    state.copy(
                        isWishlisted = prevIsWishlisted,
                        product = state.product?.copy(likeCount = prevLikeCount)
                    )
                }
                reduce { state.copy(snackbarState = SnackbarState("위시리스트 변경에 실패했습니다")) }
            }

        isWishlistInProgress = false
    }

    // 구매
    private fun showPurchaseSheet() = intent {
        reduce { state.copy(showPurchaseSheet = true, purchaseQuantity = 1) }
    }

    private fun dismissPurchaseSheet() = intent {
        reduce { state.copy(showPurchaseSheet = false) }
    }

    private fun updateQuantity(quantity: Int) = blockingIntent {  // 구매 버튼 직전 확정이 먼저 반영
        val maxSelectable = maxOf(
            1,
            minOf(state.product?.stock ?: 1, Constants.Commerce.MAX_CART_QUANTITY)
        )
        val clampedQuantity = quantity.coerceIn(1, maxSelectable)
        reduce { state.copy(purchaseQuantity = clampedQuantity) }
    }

    // 내 상품인지, 캐시가 비면 한 번 조회
    private suspend fun isOwnProduct(): Boolean {
        val myUserId = getCachedMyProfileUseCase()?.userId ?: getMyProfileUseCase().getOrNull()?.userId ?: return false
        return container.stateFlow.value.product?.creator?.id == myUserId
    }

    private fun addToCart() = intent {
        if (isOwnProduct()) {
            reduce {
                state.copy(
                    showPurchaseSheet = false,
                    snackbarState = SnackbarState("자신이 판매하는 상품은 장바구니에 담을 수 없습니다")
                )
            }
            return@intent
        }
        reduce { state.copy(isAddingToCart = true) }
        cartRepository.addToCart(productId, state.purchaseQuantity)
            .onSuccess {
                reduce {
                    state.copy(
                        isAddingToCart = false,
                        showPurchaseSheet = false,
                        snackbarState = SnackbarState("장바구니에 담았습니다", actionLabel = "보러가기")
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isAddingToCart = false,
                        showPurchaseSheet = false,
                        snackbarState = SnackbarState(error.message ?: "장바구니 추가에 실패했습니다")
                    )
                }
            }
    }

    private fun buyNow() = intent {
        if (isOwnProduct()) {
            reduce {
                state.copy(
                    showPurchaseSheet = false,
                    snackbarState = SnackbarState("자신이 판매하는 상품은 구매할 수 없습니다")
                )
            }
            return@intent
        }
        // 장바구니를 거치지 않고 이 상품·수량만 결제
        val quantity = state.purchaseQuantity
        reduce { state.copy(showPurchaseSheet = false) }
        postSideEffect(ProductDetailSideEffect.NavigateToDirectCheckout(productId, quantity))
    }

    private fun observeCartCount() {
        viewModelScope.launch {
            cartRepository.cartCount
                .collectLatest { count ->
                    intent {
                        reduce { state.copy(cartItemCount = count) }
                    }
                }
        }
    }

    private fun refreshCartCount() = intent {
        cartRepository.refreshCartCount()
    }

    // 복귀 시 재고·가격·판매 상태 조용히 갱신
    private fun refreshProduct() = intent {
        if (state.product == null) return@intent
        getProductUseCase(productId).onSuccess { product ->
            reduce { state.copy(product = product) }
        }
    }

    private fun refreshReviewPreview() = intent {
        val currentProductId = state.product?.id ?: productId
        if (currentProductId <= 0) return@intent

        val summaryResult = getReviewSummaryUseCase(currentProductId)
        val reviewsResult = getProductReviewsUseCase(currentProductId, limit = 5)

        reduce {
            state.copy(
                reviewSummary = summaryResult.getOrNull() ?: state.reviewSummary,
                recentReviews = reviewsResult.getOrNull()?.items ?: state.recentReviews
            )
        }
    }

    private fun showCouponDetailDialog() = intent {
        reduce { state.copy(showCouponDetailDialog = true) }
    }

    private fun dismissCouponDetailDialog() = intent {
        reduce { state.copy(showCouponDetailDialog = false) }
    }
}
