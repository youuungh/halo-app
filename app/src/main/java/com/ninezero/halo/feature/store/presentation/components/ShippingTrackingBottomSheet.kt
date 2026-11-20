package com.ninezero.halo.feature.store.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.copyTextToClipboard
import com.ninezero.halo.core.ui.components.bottomsheet.SheetHeader
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.ShippingProgress
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

/** 배송 조회 시트 */
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
            Spacer(modifier = Modifier.height(headerHeight + 16.dp))

            // 지금 상태 크게, 아래에 마지막 위치 · 시각
            val currentStatus = shippingStatus?.shippingStatus ?: group?.shippingStatus ?: ShippingStatus.PREPARING
            val latest = progresses.firstOrNull()
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = statusText?.takeIf { it.isNotBlank() }
                        ?: SHIPPING_STEPS.firstOrNull { it.first == currentStatus }?.second
                        ?: "준비중",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (latest != null && !isTracePending) {
                    val place = latest.location?.takeIf { it.isNotBlank() }
                    val time = latest.dateTime?.formatTrackingDateTime()
                    Text(
                        text = listOfNotNull(place, time?.let { "$it 기준" }).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ShippingTracker(shippingStatus = currentStatus)

            Spacer(modifier = Modifier.height(16.dp))

            // 택배사 · 운송장
            if (resolvedCarrier != null && resolvedTrackingNumber != null) {
                TrackingNumberRow(
                    carrier = resolvedCarrier,
                    trackingNumber = resolvedTrackingNumber,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                Text(
                    text = "배송 정보가 아직 등록되지 않았습니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "배송 추적 내역",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isTracePending) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    repeat(3) { TrackingProgressItemSkeleton() }
                }
            } else if (progresses.isEmpty()) {
                Text(
                    text = "배송 추적 내역이 아직 없어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    progresses.forEachIndexed { index, progress ->
                        TrackingProgressItem(
                            progress = progress,
                            isLatest = index == 0,
                            isLast = index == progresses.lastIndex
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }

        SheetHeader(
            title = "배송 조회",
            onClose = onDismiss,
            hazeState = hazeState,
            onHeightChange = { headerHeight = it },
            topInset = statusBarDp * morphProgress,
            handleAlpha = 1f - morphProgress
        )
    }
}

/** 택배사 · 운송장 한 줄, 오른쪽 복사 */
@Composable
private fun TrackingNumberRow(
    carrier: String,
    trackingNumber: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = carrier,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = trackingNumber.chunked(4).joinToString(" "),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .bounceClick()
                .size(40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { context.copyTextToClipboard(trackingNumber, "송장번호를 복사했습니다") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_copy),
                contentDescription = "송장번호 복사",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 내역 한 줄, 최신 점만 파랑 */
@Composable
private fun TrackingProgressItem(
    progress: ShippingProgress,
    isLatest: Boolean,
    isLast: Boolean
) {
    val timeText = progress.dateTime?.formatTrackingDateTime() ?: "-"
    val title = progress.status ?: progress.statusCode ?: "상태 업데이트"
    val detail = listOfNotNull(
        progress.location?.takeIf { it.isNotBlank() },
        progress.description?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .fillMaxHeight()
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(lineColor)
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isLatest) MaterialTheme.colorScheme.primary else lineColor)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLatest) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isLatest) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (detail.isNotEmpty()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun TrackingProgressItemSkeleton() {
    // 타임라인과 같은 짜임
    Row(modifier = Modifier.padding(bottom = 16.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(10.dp)
                .clip(CircleShape)
                .shimmer()
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .shimmer()
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .width(56.dp)
                .height(11.dp)
                .clip(MaterialTheme.shapes.extraSmall)
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
        OrderItem(1, 1, "프리미엄 블루투스 이어폰", null, 89000L,
            originalPrice = null, quantity = 1, subtotal = 89000L,
            creatorId = 10, creatorName = "TechStore", creatorAvatarUrl = null)
    ),
    totalPrice = 89000L, status = OrderStatus.CONFIRMED,
    shippingAddress = "서울시 강남구 테헤란로 123", shippingPhone = "010-1234-5678",
    shippingName = "홍길동", paymentProvider = "MOCK", paymentStatus = PaymentStatus.COMPLETED,
    memo = null, pointsUsed = 0L, couponDiscount = 0L,
    trackingNumber = "1234567890", carrier = "CJ대한통운", shippingFee = 3000L,
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
