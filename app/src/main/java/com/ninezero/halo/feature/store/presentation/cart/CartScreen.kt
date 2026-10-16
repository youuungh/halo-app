package com.ninezero.halo.feature.store.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.checkbox.HaloCheckbox
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.calcStorePricing
import com.ninezero.halo.feature.store.domain.model.isPurchasable
import com.ninezero.halo.feature.store.domain.model.toOrderLine
import com.ninezero.halo.feature.store.presentation.cart.components.CartBottomBar
import com.ninezero.halo.feature.store.presentation.cart.components.CartItemRow
import com.ninezero.halo.feature.store.presentation.cart.components.CartSkeleton
import com.ninezero.halo.feature.store.presentation.cart.components.CreatorGroupHeader
import com.ninezero.halo.feature.store.presentation.cart.components.GroupSummarySection
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
        onUpdateQuantity = { cartItemId, qty -> viewModel.onIntent(CartIntent.UpdateQuantity(cartItemId, qty)) },
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
        }
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
    onShowRestrictedContent: (String) -> Unit = {}
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
    val numberFormat = rememberKoreanNumberFormat()
    val focusManager = LocalFocusManager.current
    DisposableEffect(Unit) {
        onDispose { focusManager.clearFocus(force = true) }
    }
    val cart = state.cart
    val hasItems = cart != null && cart.items.isNotEmpty()
    val isInitialLoading = state.isLoading && cart == null
    val isPurchasable: (CartItem) -> Boolean = { item -> item.product.isPurchasable() }

    val creatorGroups = remember(cart) {
        cart?.items?.groupBy { it.product.creator.id }
            ?.map { (_, items) -> items.first().product.creator to items }
            ?: emptyList()
    }

    // 선택 가격, 구매불가 제외
    val selectedItems = remember(cart, state.selectedItemIds) {
        cart?.items?.filter { item ->
            item.id in state.selectedItemIds && isPurchasable(item)
        } ?: emptyList()
    }
    val selectedPricing = remember(selectedItems, state.creatorTierMap) {
        calcStorePricing(
            items = selectedItems.map { it.toOrderLine() },
            subscribedCreatorIds = state.creatorTierMap.keys
        )
    }
    val selectedTotalPrice = selectedPricing.subtotal
    val selectedTotalShippingFee = selectedPricing.shipping.finalShippingFee
    val selectedTotalDiscount = selectedPricing.productDiscount
    val selectedOriginalShippingFee = selectedPricing.shipping.membershipFreeShipFee
    val hasMembershipFreeShipping = selectedPricing.shipping.membershipFreeShipIds.isNotEmpty()
    val selectedOrderTotal = selectedPricing.totalBeforePoints
    val selectedItemCount = remember(selectedItems) {
        selectedItems.sumOf { it.quantity }
    }
    val isAllSelected = remember(cart, state.selectedItemIds) {
        val availableItems = cart?.items?.filter(isPurchasable) ?: emptyList()
        availableItems.isNotEmpty() && availableItems.all { it.id in state.selectedItemIds }
    }

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

                state.error != null && cart == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                !state.isLoading && !hasItems -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "장바구니가 비어있어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            val availableCreatorItemIds = items.filter(isPurchasable).map { it.id }.toSet()
                            val isCreatorAllSelected = availableCreatorItemIds.isNotEmpty() &&
                                availableCreatorItemIds.all { it in state.selectedItemIds }

                            item(key = "creator_group_${creator.id}") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = if (groupIndex == 0) 0.dp else 8.dp
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.5f
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    CreatorGroupHeader(
                                        creator = creator,
                                        isAllSelected = isCreatorAllSelected,
                                        isSubscribed = state.creatorTierMap.containsKey(creator.id),
                                        onToggleSelection = { onToggleCreatorSelection(creator.id) },
                                        onCreatorClick = { onNavigateToUserProfile(creator.id) }
                                    )

                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    items.forEachIndexed { itemIndex, cartItem ->
                                        CartItemRow(
                                            cartItem = cartItem,
                                            numberFormat = numberFormat,
                                            isSelected = cartItem.id in state.selectedItemIds,
                                            onClick = {
                                                if (cartItem.product.canAccess == false) {
                                                    onShowRestrictedContent(
                                                        "${cartItem.product.requiredTier} 멤버십 전용 상품입니다"
                                                    )
                                                } else {
                                                    onItemClick(cartItem.product.id)
                                                }
                                            },
                                            onToggleSelection = { onToggleItemSelection(cartItem.id) },
                                            onUpdateQuantity = { qty ->
                                                onUpdateQuantity(
                                                    cartItem.id,
                                                    qty
                                                )
                                            },
                                            onRemove = { onRemoveItem(cartItem.id) },
                                            bottomReservedSpace = barContentHeight
                                        )
                                        if (itemIndex < items.size - 1) {
                                            HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                    alpha = 0.3f
                                                ),
                                                modifier = Modifier.padding(
                                                    start = 52.dp,
                                                    end = 16.dp
                                                )
                                            )
                                        }
                                    }

                                    // 그룹 합계, 구매불가 제외
                                    val groupSelectedItems = items.filter { item ->
                                        item.id in state.selectedItemIds && isPurchasable(item)
                                    }
                                    if (groupSelectedItems.isNotEmpty()) {
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.5f
                                            ),
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        GroupSummarySection(
                                            selectedItems = groupSelectedItems,
                                            numberFormat = numberFormat,
                                            isCreatorSubscribed = state.creatorTierMap.containsKey(
                                                creator.id
                                            )
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
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = HaloBlurTint(backgroundColor),
                        blurRadius = 25.dp,
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                }
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
                    HaloCheckbox(
                        checked = isAllSelected,
                        onClick = onToggleSelectAll
                    )
                    Text(
                        text = "전체선택",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .offset(x = (-4).dp)
                            .weight(1f)
                    )
                    if (state.selectedItemIds.isNotEmpty()) {
                        Text(
                            text = "선택삭제",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
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
                selectedItemCount = selectedItemCount,
                selectedTotalPrice = selectedTotalPrice,
                selectedTotalShippingFee = selectedTotalShippingFee,
                selectedTotalDiscount = selectedTotalDiscount,
                selectedOriginalShippingFee = selectedOriginalShippingFee,
                hasMembershipFreeShipping = hasMembershipFreeShipping,
                selectedOrderTotal = selectedOrderTotal,
                hasSelectedItems = selectedItems.isNotEmpty(),
                numberFormat = numberFormat,
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                onCheckout = {
                    focusManager.clearFocus()  // 수량 칸 입력을 먼저 확정
                    onCheckout()
                },
                onBottomBarHeightChanged = { bottomBarHeight = it },
                onContentHeightChanged = { barContentHeight = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (hasItems) 80.dp else 0.dp)
        )

        if (state.isDeleting) {
            LoadingDialog()
        }
    }
}
