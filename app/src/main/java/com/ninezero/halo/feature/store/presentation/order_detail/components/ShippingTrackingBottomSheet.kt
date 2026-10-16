package com.ninezero.halo.feature.store.presentation.order_detail.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.ShippingProgress
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

/** 배송 조회 시트, 라이선스 시트와 같은 틀 */
@Composable
fun ShippingTrackingBottomSheet(
    group: OrderCreatorGroup?,
    shippingStatus: ShippingStatusResponse?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val statusBarPx = WindowInsets.statusBars.getTop(density)
    // 상단이 이 거리 안이면 모프 시작, 0px에서 완성
    val morphRangePx = statusBarPx + with(density) { 56.dp.toPx() }
    val morphProgress by remember(morphRangePx) {
        derivedStateOf {
            val offset = runCatching { sheetState.requireOffset() }.getOrNull() ?: Float.MAX_VALUE
            (1f - offset / morphRangePx).coerceIn(0f, 1f)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        // 도킹에 가까울수록 코너를 폄
        shape = RoundedCornerShape(
            topStart = lerp(28.dp, 0.dp, morphProgress),
            topEnd = lerp(28.dp, 0.dp, morphProgress)
        ),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) },
        modifier = modifier
    ) {
        // 펼친 상태는 뒤로가기 시 바로 접음
        BackHandler(
            enabled = sheetState.currentValue == SheetValue.Expanded && sheetState.hasPartiallyExpandedState
        ) {
            scope.launch { sheetState.partialExpand() }
        }

        ShippingTrackingContent(
            group = group,
            shippingStatus = shippingStatus,
            isLoading = isLoading,
            morphProgress = morphProgress,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ShippingTrackingContent(
    group: OrderCreatorGroup?,
    shippingStatus: ShippingStatusResponse?,
    isLoading: Boolean,
    morphProgress: Float = 0f,
    onDismiss: () -> Unit = {}
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    val statusBarDp = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    val resolvedCarrier = shippingStatus?.courierName ?: shippingStatus?.carrier ?: group?.carrier
    val resolvedTrackingNumber = shippingStatus?.trackingNumber ?: group?.trackingNumber
    val statusText = shippingStatus?.deliveryStatusText
    val lastProgressAt = shippingStatus?.lastProgressAt?.formatTrackingDateTime()
    val queriedAt = shippingStatus?.queriedAt?.formatTrackingDateTime()
    // 최신순, 못 읽는 시각은 뒤로
    val progresses = shippingStatus?.progresses
        ?.sortedByDescending { it.dateTime?.toTrackingDateTime() }
        .orEmpty()
    // 운송장이 있어야 조회 결과(상태·내역)가 더해짐
    val isTracePending = isLoading && resolvedCarrier != null && resolvedTrackingNumber != null

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()  // 위로 끌면 최상단까지 확장
                .hazeSource(hazeState)
                .blockSheetFling()
                .verticalScroll(rememberScrollState(), overscrollEffect = null)
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 8.dp))

            ShippingTracker(
                shippingStatus = shippingStatus?.shippingStatus ?: group?.shippingStatus ?: ShippingStatus.PREPARING
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "배송 상세",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (resolvedCarrier != null) {
                    InfoRow("택배사", resolvedCarrier)
                }
                if (resolvedTrackingNumber != null) {
                    InfoRow("운송장", resolvedTrackingNumber)
                }
                if (!statusText.isNullOrBlank()) {
                    InfoRow("현재 상태", statusText)
                }
                if (!lastProgressAt.isNullOrBlank()) {
                    InfoRow("최근 업데이트", lastProgressAt)
                }
                if (!queriedAt.isNullOrBlank()) {
                    InfoRow("조회 시각", queriedAt)
                }
                if (isTracePending) {
                    repeat(3) {
                        InfoRowSkeleton()
                    }
                }
                if (resolvedCarrier == null && resolvedTrackingNumber == null) {
                    Text(
                        text = "배송 정보가 아직 등록되지 않았습니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "배송 추적 내역",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (isTracePending) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    repeat(3) { index ->
                        TrackingProgressItemSkeleton()

                        if (index < 2) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            } else if (progresses.isEmpty()) {
                Text(
                    text = "배송 추적 내역이 아직 없습니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    progresses.forEachIndexed { index, progress ->
                        TrackingProgressItem(progress)

                        if (index < progresses.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = surfaceColor,
                        tint = HaloBlurTint(surfaceColor),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 0.5f,
                        endIntensity = 0f
                    )
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            // 최상단 도킹 시 상태바 인셋만큼 내려앉음
            Spacer(modifier = Modifier.height(statusBarDp * morphProgress))

            // 드래그 핸들, 모프 중 사라짐
            Box(modifier = Modifier.alpha(1f - morphProgress)) {
                BottomSheetDragHandle()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = surfaceColor,
                    size = 36.dp,
                    iconSize = 20.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "배송 조회",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Composable
private fun TrackingProgressItem(progress: ShippingProgress) {
    val timeText = progress.dateTime?.formatTrackingDateTime() ?: "-"
    val title = progress.status ?: progress.statusCode ?: "상태 업데이트"

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!progress.location.isNullOrBlank()) {
            Text(
                text = progress.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (!progress.description.isNullOrBlank()) {
            Text(
                text = progress.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Text(
            text = timeText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun TrackingProgressItemSkeleton() {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        // 상태
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
        Spacer(modifier = Modifier.height(6.dp))
        // 위치
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
        Spacer(modifier = Modifier.height(6.dp))
        // 시각
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
    }
}

// 외부 배송 API 시각
private val trackingLocalFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE)
    .optionalStart().appendLiteral('T').optionalEnd()
    .optionalStart().appendLiteral(' ').optionalEnd()
    .append(DateTimeFormatter.ISO_LOCAL_TIME)
    .toFormatter()

private val trackingDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd HH:mm")

private fun String.toTrackingDateTime(): LocalDateTime? {
    val text = trim()
    return runCatching { LocalDateTime.parse(text, trackingLocalFormatter) }.getOrNull()
        ?: runCatching {
            OffsetDateTime.parse(text).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        }.getOrNull()
}

private fun String.formatTrackingDateTime(): String =
    toTrackingDateTime()?.format(trackingDisplayFormatter) ?: this

private val previewOrder = Order(
    id = 101, orderNumber = "2026082710000137",
    items = listOf(
        OrderItem(1, 1, "프리미엄 블루투스 이어폰", null, "89000",
            originalPrice = null, quantity = 1, subtotal = "89000",
            creatorId = 10, creatorName = "TechStore", creatorAvatarUrl = null)
    ),
    totalPrice = "89000", status = "SHIPPED",
    shippingAddress = "서울시 강남구 테헤란로 123", shippingPhone = "010-1234-5678",
    shippingName = "홍길동", paymentProvider = "MOCK", paymentStatus = "COMPLETED",
    memo = null, pointsUsed = "0", couponDiscount = "0",
    trackingNumber = "1234567890", carrier = "CJ대한통운", shippingFee = "3000",
    shippingStatus = ShippingStatus.IN_TRANSIT,
    createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
)

private val previewGroup = previewOrder.creatorGroupsOrFallback().first()

private val previewOrderNoTracking = previewOrder.copy(
    creatorGroups = listOf(
        previewGroup.copy(
            trackingNumber = null,
            carrier = null,
            shippingStatus = ShippingStatus.PREPARING
        )
    )
)

@HaloLightDarkPreview
@Composable
private fun ShippingTrackingContentPreview() {
    HaloTheme {
        ShippingTrackingContent(
            group = previewGroup,
            shippingStatus = ShippingStatusResponse(
                shippingStatus = ShippingStatus.IN_TRANSIT,
                trackingNumber = "1234567890", carrier = "CJ대한통운",
                courierName = "CJ대한통운",
                deliveryStatusText = "배송중",
                lastProgressAt = "2026-02-26 10:30:00",
                queriedAt = "2026-02-26T01:35:00.000Z",
                progresses = listOf(
                    ShippingProgress(
                        dateTime = "2026-02-26 10:30:00",
                        location = "서울 성동터미널",
                        status = "집화처리",
                        description = "물품을 접수했습니다."
                    ),
                    ShippingProgress(
                        dateTime = "2026-02-26 08:40:00",
                        location = "서울 강남지점",
                        status = "배송중",
                        description = "배송 지점으로 이동 중입니다."
                    )
                )
            ),
            isLoading = false
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ShippingTrackingContentLoadingPreview() {
    HaloTheme {
        ShippingTrackingContent(
            group = previewGroup,
            shippingStatus = null,
            isLoading = true
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ShippingTrackingContentNoTrackingPreview() {
    HaloTheme {
        ShippingTrackingContent(
            group = previewOrderNoTracking.creatorGroupsOrFallback().firstOrNull(),
            shippingStatus = null,
            isLoading = false
        )
    }
}
