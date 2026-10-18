package com.ninezero.halo.feature.store.presentation.my_products

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.ProductOptionBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.feature.store.domain.model.hasLiveDeal
import com.ninezero.halo.feature.store.presentation.my_products.components.DealSetupBottomSheet
import com.ninezero.halo.feature.store.presentation.my_products.components.MyProductCard
import com.ninezero.halo.feature.store.presentation.my_products.components.SectionGroupHeader
import com.ninezero.halo.feature.store.presentation.my_products.components.myProductsSkeleton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyProductsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateProduct: () -> Unit = {},
    onNavigateToEditProduct: (Int) -> Unit = {},
    onNavigateToProductDetail: (Int) -> Unit = {},
    viewModel: MyProductsViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(MyProductsIntent.ScreenResumed)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MyProductsSideEffect.NavigateBack -> onNavigateBack()
            is MyProductsSideEffect.NavigateToCreateProduct -> onNavigateToCreateProduct()
            is MyProductsSideEffect.NavigateToEditProduct -> onNavigateToEditProduct(sideEffect.productId)
            is MyProductsSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
            is MyProductsSideEffect.ScrollToTop -> scope.launch { listState.animateScrollToItem(0) }
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(MyProductsIntent.SnackbarDismissed) }
    )

    MyProductsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        listState = listState,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun MyProductsScreenContent(
    state: MyProductsState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    listState: LazyListState = rememberLazyListState(),
    onIntent: (MyProductsIntent) -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    var headerHeight by rememberSaveableDp()

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(MyProductsIntent.LoadMore) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(MyProductsIntent.Refresh) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            when {
                state.isLoading && state.products.isEmpty() -> {
                    LazyColumn(
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        myProductsSkeleton()
                    }
                }

                state.error != null && state.products.isEmpty() -> {
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
                                onClick = { onIntent(MyProductsIntent.Load) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                state.products.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "등록된 상품이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // 그룹핑: 타임딜 → sectionTag → 미분류
                    val grouped = remember(state.products) {
                        val (dealProducts, rest) = state.products.partition { it.hasLiveDeal() }
                        val (withSection, withoutSection) = rest.partition { it.sectionTag != null }
                        val sectionGroups = withSection.groupBy { it.sectionTag!! }
                            .toSortedMap(compareBy { it.name })
                        Triple(dealProducts, sectionGroups, withoutSection)
                    }
                    val (dealProducts, sectionGroups, ungroupedProducts) = grouped
                    var colorPickerTagId by remember { mutableStateOf<Int?>(null) }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        // 타임딜: 항상 최상단 고정 섹션
                        if (dealProducts.isNotEmpty()) {
                            item(key = "header_timedeal") {
                                val chevronRotation by animateFloatAsState(
                                    targetValue = if (state.isTimeDealCollapsed) -90f else 0f,
                                    label = "timedeal_chevron"
                                )
                                Column(modifier = Modifier.animateItem()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { onIntent(MyProductsIntent.ToggleTimeDealCollapse) }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "타임딜",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${dealProducts.size}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .offset(y = 1.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(50)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            painter = painterResource(R.drawable.ic_chevron_down),
                                            contentDescription = if (state.isTimeDealCollapsed) "펼치기" else "접기",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .rotate(chevronRotation),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                }
                            }
                            if (!state.isTimeDealCollapsed) {
                                items(items = dealProducts, key = { it.id }) { product ->
                                    Column(modifier = Modifier.animateItem()) {
                                        MyProductCard(
                                            product = product,
                                            onClick = { onIntent(MyProductsIntent.NavigateToProductDetail(product.id)) },
                                            onMoreOptions = { onIntent(MyProductsIntent.ShowOptions(product)) }
                                        )
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        // 섹션 그룹별 렌더링
                        sectionGroups.forEach { (tag, products) ->
                            val isCollapsed = tag.id in state.collapsedGroups
                            val groupColor = state.groupColors[tag.id]

                            item(key = "header_${tag.id}") {
                                Column(modifier = Modifier.animateItem()) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                    SectionGroupHeader(
                                        tagName = tag.name,
                                        itemCount = products.size,
                                        isCollapsed = isCollapsed,
                                        groupColor = groupColor,
                                        onToggleCollapse = { onIntent(MyProductsIntent.ToggleGroupCollapse(tag.id)) },
                                        onColorPickerClick = {
                                            colorPickerTagId = if (colorPickerTagId == tag.id) null else tag.id
                                        },
                                        showColorPicker = colorPickerTagId == tag.id,
                                        onColorSelected = { color ->
                                            onIntent(MyProductsIntent.UpdateGroupColor(tag.id, color))
                                            colorPickerTagId = null
                                        },
                                        onDismissColorPicker = { colorPickerTagId = null }
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                }
                            }

                            if (!isCollapsed) {
                                items(
                                    items = products,
                                    key = { it.id }
                                ) { product ->
                                    Column(modifier = Modifier.animateItem()) {
                                        MyProductCard(
                                            product = product,
                                            groupColor = groupColor,
                                            onClick = { onIntent(MyProductsIntent.NavigateToProductDetail(product.id)) },
                                            onMoreOptions = { onIntent(MyProductsIntent.ShowOptions(product)) }
                                        )
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        // 미분류 상품
                        if (ungroupedProducts.isNotEmpty()) {
                            if (sectionGroups.isNotEmpty()) {
                                item(key = "header_ungrouped") {
                                    Column(modifier = Modifier.animateItem()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "미분류",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${ungroupedProducts.size}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .offset(y = 1.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                                        shape = RoundedCornerShape(50)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }

                            items(
                                items = ungroupedProducts,
                                key = { it.id }
                            ) { product ->
                                Column(modifier = Modifier.animateItem()) {
                                    MyProductCard(
                                        product = product,
                                        onClick = { onIntent(MyProductsIntent.NavigateToProductDetail(product.id)) },
                                        onMoreOptions = { onIntent(MyProductsIntent.ShowOptions(product)) }
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        if (state.isLoading && state.products.isNotEmpty()) {
                            item {
                                ListLoadMoreIndicator()
                            }
                        }
                    }
                }
            }
        }

        HazeScreenHeader(
            title = "내 스토어",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(MyProductsIntent.NavigateBack) },
            onHeightMeasured = { headerHeight = it },
            actions = {
                GlassIconButton(
                    onClick = { onIntent(MyProductsIntent.NavigateToCreateProduct) },
                    icon = painterResource(R.drawable.ic_plus),
                    contentDescription = "상품 등록",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        CenterToast(
            visible = state.toastMessage != null,
            message = state.toastMessage ?: "",
            onDismiss = { onIntent(MyProductsIntent.DismissToast) }
        )
    }

    state.optionSheetProduct?.let { product ->
        // 만료 전 딜이 설정돼 있으면 해제 가능
        val hasDeal = product.hasLiveDeal()
        ProductOptionBottomSheet(
            isDiscontinued = product.status == ProductStatus.DISCONTINUED,
            isSoldOut = product.status == ProductStatus.SOLD_OUT,
            hasDeal = hasDeal,
            onDismiss = { onIntent(MyProductsIntent.DismissOptions) },
            onSetDeal = { onIntent(MyProductsIntent.RequestSetDeal(product)) },
            onClearDeal = { onIntent(MyProductsIntent.ClearDeal(product.id)) },
            onToggleStatus = { onIntent(MyProductsIntent.ToggleProductStatus(product.id)) },
            onEdit = { onIntent(MyProductsIntent.NavigateToEditProduct(product)) },
            onDelete = { onIntent(MyProductsIntent.ShowDeleteDialog(product)) }
        )
    }

    state.dealSetupProduct?.let { product ->
        DealSetupBottomSheet(
            product = product,
            isSaving = state.isSavingDeal,
            onDismiss = { onIntent(MyProductsIntent.DismissDealSetup) },
            onConfirm = { dealPrice, startMillis, endMillis ->
                onIntent(MyProductsIntent.SetDeal(product.id, dealPrice, startMillis, endMillis))
            }
        )
    }

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "상품을 삭제하시겠어요?",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(MyProductsIntent.ConfirmDelete) },
            onDismiss = { onIntent(MyProductsIntent.DismissDeleteDialog) }
        )
    }
}
