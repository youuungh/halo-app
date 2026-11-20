package com.ninezero.halo.feature.store.presentation.product_detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.navigationBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.text.AnimatedCounter
import com.ninezero.halo.core.ui.components.viewer.MediaViewerOverlay
import com.ninezero.halo.core.ui.components.viewer.MediaViewerState
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.theme.Like
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.calcSingleCouponDiscount
import com.ninezero.halo.feature.store.domain.model.canBuy
import com.ninezero.halo.feature.store.domain.model.purchaseBlockedLabel
import com.ninezero.halo.feature.store.presentation.product_detail.components.DetailContentSection
import com.ninezero.halo.feature.store.presentation.product_detail.components.GlassPillActionButtons
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductDescriptionSection
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductDetailSkeleton
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductHeroHeight
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductHeroOverlap
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductImagePager
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductInfoSection
import com.ninezero.halo.feature.store.presentation.product_detail.components.PurchaseBottomSheet
import com.ninezero.halo.feature.store.presentation.product_detail.components.RelatedProductsSection
import com.ninezero.halo.feature.store.presentation.product_detail.components.ReviewEmptySection
import com.ninezero.halo.feature.store.presentation.product_detail.components.ReviewPreviewSection
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDirectCheckout: (productId: Int, quantity: Int) -> Unit,
    onNavigateToCart: () -> Unit = {},
    onNavigateToReviewList: (Int) -> Unit = {},
    onNavigateToProductDetail: (Int) -> Unit = {},
    onNavigateToUserProfile: (Int) -> Unit = {},
    onNavigateToChatRoom: (Int) -> Unit = {},
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(ProductDetailIntent.RefreshCartCount)
            viewModel.onIntent(ProductDetailIntent.RefreshReviewPreview)
            viewModel.onIntent(ProductDetailIntent.RefreshProduct)
        } else {
            hasInitialResumeHandled = true
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var centerToastVisible by remember { mutableStateOf(false) }
    var centerToastMessage by remember { mutableStateOf("") }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = {
            when (state.snackbarState?.actionLabel) {
                "보러가기" -> viewModel.onIntent(ProductDetailIntent.NavigateToCart)
                "재시도" -> viewModel.onIntent(ProductDetailIntent.LoadProduct)
                "채팅 열기" -> {
                    val roomId = state.navigateRoomId
                    if (roomId != null) {
                        viewModel.onIntent(ProductDetailIntent.NavigateToChatRoom(roomId))
                    }
                }
            }
        },
        onDismiss = { viewModel.onIntent(ProductDetailIntent.SnackbarDismissed) }
    )

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProductDetailSideEffect.NavigateBack -> onNavigateBack()
            is ProductDetailSideEffect.NavigateToCart -> onNavigateToCart()
            is ProductDetailSideEffect.NavigateToChatRoom -> onNavigateToChatRoom(sideEffect.roomId)
            is ProductDetailSideEffect.NavigateToDirectCheckout -> onNavigateToDirectCheckout(sideEffect.productId, sideEffect.quantity)
            is ProductDetailSideEffect.NavigateToReviewList -> onNavigateToReviewList(sideEffect.productId)
            is ProductDetailSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(
                sideEffect.productId
            )

            is ProductDetailSideEffect.ShowCenterToast -> {
                centerToastMessage = sideEffect.message
                centerToastVisible = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ProductDetailScreenContent(
            state = state,
            onIntent = viewModel::onIntent,
            onNavigateToUserProfile = onNavigateToUserProfile
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            bottomBarPadding = if (state.product == null) 0.dp else 60.dp  // 버튼 48 + 아래 여백 12
        )

        // 이미지 뷰어 오버레이
        MediaViewerOverlay(
            state = MediaViewerState(
                isVisible = state.isImageViewerVisible,
                items = state.imageViewerUrls.map { ViewerMediaItem(url = it) },
                initialIndex = state.imageViewerInitialIndex
            ),
            onDismiss = { viewModel.onIntent(ProductDetailIntent.DismissImageViewer) }
        )
    }

    CenterToast(
        visible = centerToastVisible,
        message = centerToastMessage,
        onDismiss = { centerToastVisible = false }
    )

    ShareSheetHost(
        visible = state.showShareSheet && state.product != null,
        shareAction = { shareViewModel -> state.product?.let { shareViewModel.onIntent(ShareIntent.ShareProduct(it.id)) } },
        onDismiss = { viewModel.onIntent(ProductDetailIntent.DismissShareSheet) },
        onShareSuccess = { roomIds, count ->
            viewModel.onIntent(
                ProductDetailIntent.ShareCompleted(
                    roomIds,
                    count
                )
            )
        },
        externalLink = state.product?.let { DeepLinks.productLink(it.id) }
    )
}

/** 스크롤 반응형 상단 오버레이 */
@Composable
private fun BoxScope.ProductDetailTopOverlay(  // 스크롤 읽기를 여기 가둬 본문 recomposition 방지
    scrollState: ScrollState,
    collapseThresholdPx: Float,
    backgroundColor: Color,
    hazeState: HazeState,
    statusBarHeight: Dp,
    cartItemCount: Int,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(statusBarHeight + 56.dp)
            // 스크롤 값은 그리기 단계에서만 읽음
            .graphicsLayer {
                val progress = if (collapseThresholdPx > 0f) {
                    1f - (scrollState.value.toFloat() / collapseThresholdPx).coerceIn(0f, 1f)
                } else 0f
                alpha = ((0.7f - progress) / 0.7f).coerceIn(0f, 1f)
            }
            .haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))
    )

    val buttonIconTint = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .statusBarsPaddingIgnoringVisibility()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 뒤로가기 버튼
        GlassIconButton(
            onClick = onNavigateBack,
            icon = painterResource(R.drawable.ic_chevron_left),
            contentDescription = "뒤로가기",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            iconTint = buttonIconTint
        )

        GlassPillActionButtons(
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            iconTint = buttonIconTint,
            cartItemCount = cartItemCount,
            onCartClick = onNavigateToCart,
            onShareClick = onShare
        )
    }
}

@Composable
internal fun ProductDetailScreenContent(
    state: ProductDetailState,
    onIntent: (ProductDetailIntent) -> Unit,
    onNavigateToUserProfile: (Int) -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    val scrollState = rememberScrollState()

    val statusBarHeight = with(density) {
        // 뷰어가 바를 숨겨도 높이 유지
        WindowInsets.statusBarsIgnoringVisibility.getTop(this).toDp()
    }

    // 스크롤 값은 오버레이에서만 읽어 recomposition 회피
    val imageHeightPx = with(density) { ProductHeroHeight.toPx() }
    val topBarHeightPx = with(density) { (statusBarHeight + 56.dp).toPx() }
    val collapseThresholdPx = imageHeightPx - topBarHeightPx

    // 버튼은 스켈레톤부터 같은 것을 유지하고 내용만 교체
    // 바탕을 소스 밖에 칠해야 유리 버튼이 빈 곳에 진한 색을 냄
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val product = state.product
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                ProductDetailSkeleton()
            }
        } else {
            ProductDetailBody(
                product = product,
                state = state,
                hazeState = hazeState,
                scrollState = scrollState,
                backgroundColor = backgroundColor,
                onIntent = onIntent,
                onNavigateToUserProfile = onNavigateToUserProfile
            )
        }

        ProductDetailTopOverlay(
            scrollState = scrollState,
            collapseThresholdPx = collapseThresholdPx,
            backgroundColor = backgroundColor,
            hazeState = hazeState,
            statusBarHeight = statusBarHeight,
            cartItemCount = state.cartItemCount,
            onNavigateBack = { onIntent(ProductDetailIntent.NavigateBack) },
            onNavigateToCart = { onIntent(ProductDetailIntent.NavigateToCart) },
            // 공유할 상품 정보가 있어야 동작
            onShare = { if (product != null) onIntent(ProductDetailIntent.ShareProduct) }
        )
    }
}

/** 내 찜 상태와 전체 찜 수를 함께 보여주는 버튼 */
@Composable
private fun WishlistButton(
    isWishlisted: Boolean,
    count: Int,
    hazeState: HazeState,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isWishlisted) Like else MaterialTheme.colorScheme.onSurface,
        label = "wishlistColor"
    )
    Row(
        modifier = Modifier
            .bounceClick()
            .height(48.dp)
            .clip(CircleShape)
            .haloBlur(hazeState, HaloBlurs.glassField(backgroundColor))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(start = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            painter = painterResource(if (isWishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart),
            contentDescription = if (isWishlisted) "위시리스트에서 제거" else "위시리스트에 추가",
            modifier = Modifier.size(22.dp),
            tint = contentColor
        )
        AnimatedCounter(
            count = count,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )
    }
}

/** 큰 영역을 나누는 띠 */
@Composable
private fun DetailSectionBand() {
    Spacer(modifier = Modifier.height(28.dp))
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun ProductDetailBody(
    product: Product,
    state: ProductDetailState,
    hazeState: HazeState,
    scrollState: ScrollState,
    backgroundColor: Color,
    onIntent: (ProductDetailIntent) -> Unit,
    onNavigateToUserProfile: (Int) -> Unit
) {
    val couponAppliedPrice = remember(state.bestCoupon, product.price) {
        val bestCoupon = state.bestCoupon ?: return@remember null
        val productPrice = product.price.toInt()
        val discount = calcSingleCouponDiscount(bestCoupon, productPrice.toLong()).toInt()
        if (discount > 0) productPrice - discount else null
    }

    val onRelatedProductClick: (Product) -> Unit = { related ->
        if (related.canAccess == false) {
            onIntent(
                ProductDetailIntent.ShowRestrictedContentSnackbar(
                    "${related.requiredTier.label} 멤버십 전용 상품입니다"
                )
            )
        } else {
            onIntent(ProductDetailIntent.NavigateToProductDetail(related.id))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(scrollState)
        ) {
            // 사진 아래쪽이 본문 뒤로 겹치도록 영역을 줄임
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ProductHeroHeight - ProductHeroOverlap)
                    .wrapContentHeight(align = Alignment.Top, unbounded = true)
            ) {
                ProductImagePager(
                    imageUrls = product.imageUrls,
                    onImageClick = { index ->
                        onIntent(ProductDetailIntent.ShowImageViewer(product.imageUrls, index))
                    },
                    parallaxOffset = { scrollState.value * 0.5f }
                )
            }

            // 본문
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ProductInfoSection(
                    product = product,
                    reviewSummary = state.reviewSummary,
                    bestCoupon = state.bestCoupon,
                    couponAppliedPrice = couponAppliedPrice,
                    isCouponDetailExpanded = state.isCouponDetailExpanded,
                    userTierInfo = state.userTierInfo,
                    onCreatorClick = { onNavigateToUserProfile(product.creator.id) },
                    onReviewClick = {
                        onIntent(ProductDetailIntent.NavigateToReviewList(product.id))
                    },
                    onCouponDetailToggle = {
                        onIntent(ProductDetailIntent.ToggleCouponDetail)
                    }
                )

                DetailSectionBand()

                // 리뷰 프리뷰
                if (state.recentReviews.isNotEmpty()) {
                    ReviewPreviewSection(
                        recentReviews = state.recentReviews,
                        totalReviews = state.reviewSummary?.totalReviews ?: state.recentReviews.size,
                        onViewAllClick = {
                            onIntent(ProductDetailIntent.NavigateToReviewList(product.id))
                        }
                    )
                } else if (!state.isLoading) {
                    // 리뷰 없음, 로딩 끝난 뒤만
                    ReviewEmptySection()
                }

                if (product.description.isNotBlank() || product.detailContent.isNotEmpty()) {
                    DetailSectionBand()
                }

                // 상품 설명
                if (product.description.isNotBlank()) {
                    ProductDescriptionSection(
                        description = product.description,
                        isExpanded = state.isInfoExpanded,
                        onToggleExpanded = { onIntent(ProductDetailIntent.ToggleInfoExpanded) }
                    )
                }

                // 상세 컨텐츠 블럭
                if (product.detailContent.isNotEmpty()) {
                    DetailContentSection(
                        detailContent = product.detailContent,
                        isExpanded = state.isDetailExpanded,
                        onToggleExpanded = { onIntent(ProductDetailIntent.ToggleDetailExpanded) }
                    )
                }

                // 크리에이터의 다른 상품
                if (state.creatorProducts.isNotEmpty()) {
                    DetailSectionBand()
                    RelatedProductsSection(
                        title = "판매자의 다른 상품",
                        products = state.creatorProducts,
                        onProductClick = onRelatedProductClick
                    )
                }

                // 연관 상품
                if (state.relatedProducts.isNotEmpty()) {
                    DetailSectionBand()
                    RelatedProductsSection(
                        title = "연관 상품",
                        products = state.relatedProducts,
                        onProductClick = onRelatedProductClick
                    )
                }

                // 하단 바 여백
                Spacer(
                    modifier = Modifier
                        .navigationBarsPaddingIgnoringVisibility()
                        .height(72.dp)
                )
            }
        }

        // 하단 바
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .haloBlur(hazeState, HaloBlurs.fadeUp(backgroundColor))
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .navigationBarsPaddingIgnoringVisibility()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val isAvailable = product.canBuy()
            val disabledText = product.purchaseBlockedLabel()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WishlistButton(
                    isWishlisted = state.isWishlisted,
                    count = product.likeCount,
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    onClick = { onIntent(ProductDetailIntent.ToggleWishlist) }
                )

                Spacer(modifier = Modifier.width(12.dp))

                PrimaryButton(
                    onClick = { onIntent(ProductDetailIntent.ShowPurchaseSheet) },
                    enabled = isAvailable,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = disabledText ?: "구매하기",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (state.showPurchaseSheet) {
            PurchaseBottomSheet(
                product = product,
                quantity = state.purchaseQuantity,
                isAddingToCart = state.isAddingToCart,
                userTierInfo = state.userTierInfo,
                onDismiss = { onIntent(ProductDetailIntent.DismissPurchaseSheet) },
                onQuantityChange = { onIntent(ProductDetailIntent.UpdateQuantity(it)) },
                onAddToCart = { onIntent(ProductDetailIntent.AddToCart) },
                onBuyNow = { onIntent(ProductDetailIntent.BuyNow) }
            )
        }
    }
}
