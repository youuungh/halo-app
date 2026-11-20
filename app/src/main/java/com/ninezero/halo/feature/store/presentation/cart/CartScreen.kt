package com.ninezero.halo.feature.store.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.list.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.list.ListEmptyState
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.components.toggle.HaloCheckbox
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.calcStorePricing
import com.ninezero.halo.feature.store.domain.model.canBuy
import com.ninezero.halo.feature.store.domain.model.earnRatePercent
import com.ninezero.halo.feature.store.domain.model.toOrderLine
import com.ninezero.halo.feature.store.presentation.cart.components.CartBarSummary
import com.ninezero.halo.feature.store.presentation.cart.components.CartBottomBar
import com.ninezero.halo.feature.store.presentation.cart.components.CartItemRow
import com.ninezero.halo.feature.store.presentation.cart.components.CartSkeleton
import com.ninezero.halo.feature.store.presentation.cart.components.CreatorGroupHeader
import com.ninezero.halo.feature.store.presentation.cart.components.FreeShippingNotice
import com.ninezero.halo.feature.store.presentation.cart.components.GroupSummarySection
import com.ninezero.halo.feature.store.presentation.components.calcMembershipBreakdown
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (List<Int>) -> Unit,
    onNavigateToProductDetail: (Int) -> Unit = {},
    onNavigateToUserProfile: (Int) -> Unit = {},
    viewModel: CartViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(CartIntent.Refresh)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CartSideEffect.NavigateBack -> onNavigateBack()
            is CartSideEffect.NavigateToCheckout -> onNavigateToCheckout(sideEffect.selectedItemIds)
            is CartSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
            is CartSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(CartIntent.SnackbarDismissed) }
    )

    CartScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.onIntent(CartIntent.Refresh) },
        onNavigateBack = { viewModel.onIntent(CartIntent.NavigateBack) },
        onUpdateQuantity = { cartItemId, quantity -> viewModel.onIntent(CartIntent.UpdateQuantity(cartItemId, quantity)) },
        onRemoveItem = { cartItemId -> viewModel.onIntent(CartIntent.RemoveItem(cartItemId)) },
        onItemClick = { productId -> viewModel.onIntent(CartIntent.NavigateToProductDetail(productId)) },
        onCheckout = { viewModel.onIntent(CartIntent.NavigateToCheckout) },
        onRetry = { viewModel.onIntent(CartIntent.LoadCart) },
        onToggleItemSelection = { cartItemId -> viewModel.onIntent(CartIntent.ToggleItemSelection(cartItemId)) },
        onToggleCreatorSelection = { creatorId -> viewModel.onIntent(CartIntent.ToggleCreatorSelection(creatorId)) },
        onToggleSelectAll = { viewModel.onIntent(CartIntent.ToggleSelectAll) },
        onRemoveSelectedItems = { viewModel.onIntent(CartIntent.RemoveSelectedItems) },
        onNavigateToUserProfile = { userId -> viewModel.onIntent(CartIntent.NavigateToUserProfile(userId)) },
        onShowRestrictedContent = { message ->
            viewModel.onIntent(CartIntent.ShowRestrictedContentSnackbar(message))
        },
        onQuantityLimit = { max -> viewModel.onIntent(CartIntent.ShowQuantityLimitSnackbar(max)) }
    )
}

@Composable
internal fun CartScreenContent(
    state: CartState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRefresh: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onUpdateQuantity: (Int, Int) -> Unit = { _, _ -> },
    onRemoveItem: (Int) -> Unit = {},
    onItemClick: (Int) -> Unit = {},
    onCheckout: () -> Unit = {},
    onRetry: () -> Unit = {},
    onToggleItemSelection: (Int) -> Unit = {},
    onToggleCreatorSelection: (Int) -> Unit = {},
    onToggleSelectAll: () -> Unit = {},
    onRemoveSelectedItems: () -> Unit = {},
    onNavigateToUserProfile: (Int) -> Unit = {},
    onShowRestrictedContent: (String) -> Unit = {},
    onQuantityLimit: (Int) -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()
    // 주문바 높이 보존, 리셋 시 스크롤 클램프
    var bottomBarHeight by rememberSaveableDp()
    // 인셋 제외 바텀바 높이, 수량필드 스크롤 보정용
    var barContentHeight by remember { mutableStateOf(0.dp) }
    // 스낵바용: 펼침·접힘 애니메이션 중 지금 보이는 카드 높이
    var barVisibleHeight by remember { mutableStateOf(0.dp) }
    val numberFormat = rememberKoreanNumberFormat()
    val focusManager = LocalFocusManager.current
    DisposableEffect(Unit) {
        onDispose { focusManager.clearFocus(force = true) }
    }
    val cart = state.cart
    val hasItems = cart != null && cart.items.isNotEmpty()
    val isInitialLoading = state.isLoading && cart == null
    val canBuy: (CartItem) -> Boolean = { item -> item.product.canBuy() }

    val creatorGroups = remember(cart) {
        cart?.items?.groupBy { it.product.creator.id }
            ?.map { (_, items) -> items.first().product.creator to items }
            ?: emptyList()
    }

    // 선택 가격, 구매불가 제외
    val selectedItems = remember(cart, state.selectedItemIds) {
        cart?.items?.filter { item ->
            item.id in state.selectedItemIds && canBuy(item)
        } ?: emptyList()
    }
    val barSummary = remember(selectedItems, state.creatorTierMap) {
        buildCartBarSummary(selectedItems, state.creatorTierMap)
    }
    val availableCount = remember(cart) { cart?.items?.count(canBuy) ?: 0 }
    val isAllSelected = availableCount > 0 && selectedItems.size == availableCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            // 빈 영역 탭 시 포커스 해제
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            when {
                isInitialLoading -> {
                    CartSkeleton(topPadding = headerHeight)
                }

                // 다른 목록 화면처럼 화면 가운데
                state.error != null && cart == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ListErrorState(
                            title = "장바구니를 불러오지 못했습니다",
                            reason = state.error,
                            onRetry = onRetry
                        )
                    }
                }

                !state.isLoading && !hasItems -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ListEmptyState(
                            title = "장바구니가 비어있어요",
                            description = "마음에 드는 상품을 담아보세요"
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = bottomBarHeight)
                    ) {
                        creatorGroups.forEachIndexed { groupIndex, (creator, items) ->
                            val availableCreatorItemIds = items.filter(canBuy).map { it.id }.toSet()
                            val isCreatorAllSelected = availableCreatorItemIds.isNotEmpty() &&
                                availableCreatorItemIds.all { it in state.selectedItemIds }

                            val isSubscribed = state.creatorTierMap.containsKey(creator.id)
                            // 그룹 합계, 구매불가 제외
                            val groupSelectedItems = items.filter { item ->
                                item.id in state.selectedItemIds && canBuy(item)
                            }
                            val cardShape = RoundedCornerShape(20.dp)

                            item(key = "creator_group_${creator.id}") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = if (groupIndex == 0) 0.dp else 12.dp
                                        )
                                        .then(
                                            if (isSubscribed) {
                                                Modifier
                                                    .background(MembershipColors.groupCardBrush, cardShape)
                                                    .border(0.5.dp, MembershipColors.groupCardBorder, cardShape)
                                            } else {
                                                Modifier.cardBorder(cardShape)
                                            }
                                        )
                                        .clip(cardShape)
                                        .padding(top = 2.dp, bottom = 16.dp)
                                ) {
                                    CreatorGroupHeader(
                                        creator = creator,
                                        isAllSelected = isCreatorAllSelected,
                                        isSubscribed = isSubscribed,
                                        onToggleSelection = { onToggleCreatorSelection(creator.id) },
                                        onCreatorClick = { onNavigateToUserProfile(creator.id) }
                                    )

                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    // 멤버십은 항상 무료배송이라 안내 없음
                                    if (!isSubscribed && groupSelectedItems.isNotEmpty()) {
                                        FreeShippingNotice(
                                            subtotal = groupSelectedItems.sumOf { it.toOrderLine().subtotal },
                                            numberFormat = numberFormat
                                        )
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }

                                    items.forEachIndexed { itemIndex, cartItem ->
                                        CartItemRow(
                                            cartItem = cartItem,
                                            numberFormat = numberFormat,
                                            isSelected = cartItem.id in state.selectedItemIds,
                                            onClick = {
                                                if (cartItem.product.canAccess == false) {
                                                    onShowRestrictedContent(
                                                        "${cartItem.product.requiredTier.label} 멤버십 전용 상품입니다"
                                                    )
                                                } else {
                                                    onItemClick(cartItem.product.id)
                                                }
                                            },
                                            onToggleSelection = { onToggleItemSelection(cartItem.id) },
                                            onUpdateQuantity = { quantity ->
                                                onUpdateQuantity(
                                                    cartItem.id,
                                                    quantity
                                                )
                                            },
                                            onQuantityLimit = onQuantityLimit,
                                            onRemove = { onRemoveItem(cartItem.id) },
                                            isMembershipCard = isSubscribed,
                                            bottomReservedSpace = barContentHeight,
                                            modifier = Modifier.padding(top = if (itemIndex == 0) 14.dp else 20.dp)
                                        )
                                    }

                                    if (groupSelectedItems.isNotEmpty()) {
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 16.dp,
                                                bottom = 12.dp
                                            )
                                        )
                                        GroupSummarySection(
                                            selectedItems = groupSelectedItems,
                                            numberFormat = numberFormat,
                                            earnRatePercent = earnRatePercent(state.creatorTierMap[creator.id]?.tier),
                                            isCreatorSubscribed = isSubscribed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Haze 헤더 오버레이
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(
                    hazeState,
                    HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = backgroundColor,
                        blurRadius = 25.dp,
                        noiseFactor = 0f,
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0.7f,
                            endIntensity = 0f
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            // 상단 앱바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GlassIconButton(
                    onClick = onNavigateBack,
                    icon = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "장바구니",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 선택 바
            // 스켈레톤 중에도 렌더해 headerHeight 고정
            if (hasItems || isInitialLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.toggleable(
                            value = isAllSelected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Checkbox,
                            onValueChange = { onToggleSelectAll() }
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 체크는 48 터치 칸 자리, 글자 쪽만 11로 붙임
                        HaloCheckbox(
                            checked = isAllSelected,
                            modifier = Modifier.padding(start = 15.dp, top = 15.dp, end = 11.dp, bottom = 15.dp)
                        )
                        Text(
                            text = "전체선택",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (availableCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${selectedItems.size}/$availableCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (state.selectedItemIds.isNotEmpty()) {
                        Text(
                            text = "선택삭제",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onRemoveSelectedItems() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 하단 바
        if (hasItems) {
            CartBottomBar(
                summary = barSummary,
                numberFormat = numberFormat,
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                onCheckout = {
                    focusManager.clearFocus()  // 수량 칸 입력을 먼저 확정
                    onCheckout()
                },
                onBottomBarHeightChanged = { bottomBarHeight = it },
                onContentHeightChange = { barVisibleHeight = it },
                onContentHeightChanged = { barContentHeight = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        BottomBarSnackbarHost(
            hostState = snackbarHostState,
            bottomPadding = { if (hasItems) barVisibleHeight else 0.dp },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (state.isDeleting) {
            LoadingDialog()
        }
    }
}

/** 선택 상품의 하단 바 가격 내역 */
private fun buildCartBarSummary(
    selectedItems: List<CartItem>,
    creatorTierMap: Map<Int, UserTierInfo>
): CartBarSummary {
    val orderLines = selectedItems.map { it.toOrderLine() }
    val pricing = calcStorePricing(items = orderLines, subscribedCreatorIds = creatorTierMap.keys)
    val membership = calcMembershipBreakdown(
        items = orderLines,
        pricing = pricing,
        subscribedCreatorIds = creatorTierMap.keys,
        tierOf = { creatorTierMap[it]?.tier }
    )
    return CartBarSummary(
        itemCount = selectedItems.size,
        productAmount = pricing.subtotal + pricing.productDiscount,
        productDiscount = pricing.productDiscount,
        shippingFee = pricing.shipping.finalShippingFee + membership.freeShippingAmount,
        orderTotal = pricing.totalBeforePoints,
        membership = membership
    )
}

/** 바 윗선을 따라가는 스낵바, 높이는 여기서만 읽음 */
@Composable
private fun BottomBarSnackbarHost(
    hostState: SnackbarHostState,
    bottomPadding: () -> Dp,
    modifier: Modifier = Modifier
) {
    DefaultSnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(bottom = bottomPadding())
    )
}
