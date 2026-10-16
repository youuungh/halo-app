package com.ninezero.halo.feature.store.presentation.product_detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.media_viewer.MediaViewerOverlay
import com.ninezero.halo.core.ui.components.media_viewer.MediaViewerState
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.navigationBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import com.ninezero.halo.feature.store.domain.model.calcSingleCouponDiscount
import com.ninezero.halo.feature.store.domain.model.isPurchasable
import com.ninezero.halo.feature.store.domain.model.purchaseBlockedLabel
import com.ninezero.halo.feature.store.presentation.product_detail.components.DetailContentSection
import com.ninezero.halo.feature.store.presentation.product_detail.components.GlassPillActionButtons
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductDescriptionSection
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductDetailSkeleton
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductDetailTabRow
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductImagePager
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductInfoSection
import com.ninezero.halo.feature.store.presentation.product_detail.components.PurchaseBottomSheet
import com.ninezero.halo.feature.store.presentation.product_detail.components.RelatedProductsSection
import com.ninezero.halo.feature.store.presentation.product_detail.components.ReviewEmptySection
import com.ninezero.halo.feature.store.presentation.product_detail.components.ReviewPreviewSection
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.Like
import dev.chrisbanes.haze.HazeProgressive
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
        when {
            state.product == null -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    ProductDetailSkeleton()
                    GlassIconButton(
                        onClick = { viewModel.onIntent(ProductDetailIntent.NavigateBack) },
                        icon = painterResource(R.drawable.ic_chevron_left),
                        contentDescription = "뒤로가기",
                        modifier = Modifier
                            .statusBarsPaddingIgnoringVisibility()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }

            else -> {
                ProductDetailScreenContent(
                    state = state,
                    onIntent = viewModel::onIntent,
                    onNavigateToUserProfile = onNavigateToUserProfile
                )
            }
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            bottomBarPadding = if (state.product == null) 0.dp else 64.dp
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
        shareAction = { vm -> vm.onIntent(ShareIntent.ShareProduct(state.product!!.id)) },
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
    val progress = if (collapseThresholdPx > 0f) {
        1f - (scrollState.value.toFloat() / collapseThresholdPx).coerceIn(0f, 1f)
    } else 0f

    val topAppBarAlpha = ((0.7f - progress) / 0.7f).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(statusBarHeight + 80.dp)
            .graphicsLayer { alpha = topAppBarAlpha }
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = Color.Transparent,
                    tint = HaloBlurTint(backgroundColor.copy(alpha = 0.7f)),
                    noiseFactor = 0f
                )
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = 0.5f,
                    endIntensity = 0f
                )
            }
    )

    // 네비게이션 버튼
    val buttonBgColor = lerp(
        start = backgroundColor,
        stop = Color(0xFFBBBBBB),
        fraction = progress
    )
    val buttonIconTint = lerp(
        start = MaterialTheme.colorScheme.onSurface,
        stop = Color.White,
        fraction = progress
    )

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
            backgroundColor = buttonBgColor,
            iconTint = buttonIconTint
        )

        GlassPillActionButtons(
            hazeState = hazeState,
            backgroundColor = buttonBgColor,
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
    val product = state.product ?: return
    val backgroundColor = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    val scrollState = rememberScrollState()
    val numberFormat = rememberKoreanNumberFormat()
    val earnPoints = remember(product.price) {
        (product.price.toIntOrNull() ?: 0) / 100
    }
    val couponAppliedPrice = remember(state.bestCoupon, product.price) {
        val bestCoupon = state.bestCoupon ?: return@remember null
        val productPrice = product.price.toIntOrNull() ?: return@remember null
        val discount = calcSingleCouponDiscount(bestCoupon, productPrice.toLong()).toInt()
        if (discount > 0) productPrice - discount else null
    }

    val statusBarHeight = with(density) {
        // 뷰어가 바를 숨겨도 높이 유지
        WindowInsets.statusBarsIgnoringVisibility.getTop(this).toDp()
    }

    // 스크롤 값은 오버레이에서만 읽어 recomposition 회피
    val imageHeightPx = with(density) { 400.dp.toPx() }
    val topBarHeightPx = with(density) { (statusBarHeight + 56.dp).toPx() }
    val collapseThresholdPx = imageHeightPx - topBarHeightPx

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(scrollState)
        ) {
            // 이미지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .graphicsLayer {
                        // draw 단계에서 직접 읽음
                        translationY = scrollState.value * 0.5f
                    }
            ) {
                ProductImagePager(
                    imageUrls = product.imageUrls,
                    onImageClick = { index ->
                        onIntent(ProductDetailIntent.ShowImageViewer(product.imageUrls, index))
                    },
                    viewCount = product.viewCount,
                    likeCount = product.likeCount
                )
            }

            // 본문
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = backgroundColor
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProductInfoSection(
                        product = product,
                        reviewSummary = state.reviewSummary,
                        bestCoupon = state.bestCoupon,
                        couponAppliedPrice = couponAppliedPrice,
                        showCouponDetailPopup = state.showCouponDetailDialog,
                        onCreatorClick = { onNavigateToUserProfile(product.creator.id) },
                        onReviewClick = {
                            onIntent(ProductDetailIntent.NavigateToReviewList(product.id))
                        },
                        onCouponDetailClick = {
                            onIntent(ProductDetailIntent.ShowCouponDetailDialog)
                        },
                        onDismissCouponDetail = {
                            onIntent(ProductDetailIntent.DismissCouponDetailDialog)
                        }
                    )

                    // 혜택 정보
                    if (earnPoints > 0) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        val shippingFee = product.shippingFee.toIntOrNull() ?: 0
                        val freeShippingThreshold = Constants.Commerce.FREE_SHIPPING_THRESHOLD
                        val benefitTextStyle = MaterialTheme.typography.bodySmall
                        val benefitIconColor = MaterialTheme.colorScheme.onSurface
                        val benefitTextColor = MaterialTheme.colorScheme.onSurface
                        val benefitHighlightColor = MaterialTheme.colorScheme.primary
                        val userTierInfo = state.userTierInfo
                        val isSubscribed = userTierInfo?.isSubscribed == true
                        val tierEarnRate = when (userTierInfo?.tier) {
                            SubscriptionPlanTier.TIER1 -> 5
                            SubscriptionPlanTier.TIER2 -> 10
                            else -> 1
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 포인트 적립
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_point),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = benefitIconColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isSubscribed) {
                                    val membershipEarnPoints =
                                        (product.price.toIntOrNull() ?: 0) * tierEarnRate / 100
                                    Text(
                                        text = buildAnnotatedString {
                                            append("구매 시 ")
                                            withStyle(
                                                SpanStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = benefitHighlightColor
                                                )
                                            ) {
                                                append("${numberFormat.format(membershipEarnPoints)}P")
                                            }
                                            append(" 적립 (${tierEarnRate}%)")
                                        },
                                        style = benefitTextStyle,
                                        color = benefitTextColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = HaloGradient,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 5.dp)
                                    ) {
                                        Text(
                                            text = "멤버십",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "구매 시 ${numberFormat.format(earnPoints)}P 적립 (1%)",
                                        style = benefitTextStyle,
                                        color = benefitTextColor
                                    )
                                }
                            }

                            // 배송비
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_shipping),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = benefitIconColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isSubscribed) {
                                    Text(
                                        text = buildAnnotatedString {
                                            if (shippingFee > 0) {
                                                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                                                    append("${numberFormat.format(shippingFee)}원")
                                                }
                                                append(" ")
                                            }
                                            withStyle(
                                                SpanStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = benefitHighlightColor
                                                )
                                            ) {
                                                append("무료배송")
                                            }
                                        },
                                        style = benefitTextStyle,
                                        color = benefitTextColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = HaloGradient,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 5.dp)
                                    ) {
                                        Text(
                                            text = "멤버십",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else if (shippingFee > 0) {
                                    Text(
                                        text = "배송비 ${numberFormat.format(shippingFee)}원 (${
                                            numberFormat.format(
                                                freeShippingThreshold
                                            )
                                        }원 이상 무료배송)",
                                        style = benefitTextStyle,
                                        color = benefitTextColor
                                    )
                                } else {
                                    Text(
                                        text = "무료배송",
                                        style = benefitTextStyle,
                                        color = benefitTextColor
                                    )
                                }
                            }
                        }
                    }

                    // 리뷰 프리뷰
                    if (state.recentReviews.isNotEmpty()) {
                        ReviewPreviewSection(
                            recentReviews = state.recentReviews,
                            onViewAllClick = {
                                onIntent(ProductDetailIntent.NavigateToReviewList(product.id))
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    } else if (!state.isLoading) {
                        // 리뷰 없음, 로딩 끝난 뒤만
                        ReviewEmptySection()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    // 상세 탭
                    ProductDetailTabRow(
                        selectedTab = state.selectedDetailTab,
                        onTabSelected = { onIntent(ProductDetailIntent.SelectDetailTab(it)) }
                    )

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
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )

                        RelatedProductsSection(
                            title = "판매자의 다른 상품",
                            products = state.creatorProducts,
                            onProductClick = { product ->
                                if (product.canAccess == false) {
                                    onIntent(
                                        ProductDetailIntent.ShowRestrictedContentSnackbar(
                                            "${product.requiredTier} 멤버십 전용 상품입니다"
                                        )
                                    )
                                } else {
                                    onIntent(ProductDetailIntent.NavigateToProductDetail(product.id))
                                }
                            }
                        )
                    }

                    // 연관 상품
                    if (state.relatedProducts.isNotEmpty()) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )

                        RelatedProductsSection(
                            title = "연관 상품",
                            products = state.relatedProducts,
                            onProductClick = { product ->
                                if (product.canAccess == false) {
                                    onIntent(
                                        ProductDetailIntent.ShowRestrictedContentSnackbar(
                                            "${product.requiredTier} 멤버십 전용 상품입니다"
                                        )
                                    )
                                } else {
                                    onIntent(ProductDetailIntent.NavigateToProductDetail(product.id))
                                }
                            }
                        )
                    }

                    // 하단 바 여백
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
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
            onShare = { onIntent(ProductDetailIntent.ShareProduct) }
        )

        // 하단 바
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = HaloBlurTint(backgroundColor),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 0f,
                        endIntensity = 0.5f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .navigationBarsPaddingIgnoringVisibility()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val isAvailable = product.isPurchasable()
            val disabledText = product.purchaseBlockedLabel()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = { onIntent(ProductDetailIntent.ToggleWishlist) },
                    icon = painterResource(
                        if (state.isWishlisted) R.drawable.ic_heart_filled
                        else R.drawable.ic_heart
                    ),
                    contentDescription = "위시리스트",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    iconTint = if (state.isWishlisted) {
                        Like
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                GlassActionButton(
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
                onDismiss = { onIntent(ProductDetailIntent.DismissPurchaseSheet) },
                onQuantityChange = { onIntent(ProductDetailIntent.UpdateQuantity(it)) },
                onAddToCart = { onIntent(ProductDetailIntent.AddToCart) },
                onBuyNow = { onIntent(ProductDetailIntent.BuyNow) }
            )
        }
    }
}
