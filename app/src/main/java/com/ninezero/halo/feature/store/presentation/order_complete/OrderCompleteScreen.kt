package com.ninezero.halo.feature.store.presentation.order_complete

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.copyTextToClipboard
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.feature.store.domain.model.Order
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun OrderCompleteScreen(
    orderId: Int,
    orderNumber: String,
    onNavigateToOrderDetail: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: OrderCompleteViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value

    OrderCompleteScreenContent(
        orderNumber = orderNumber,
        order = state.order,
        onNavigateToOrderDetail = { onNavigateToOrderDetail(orderId) },
        onNavigateToHome = onNavigateToHome
    )
}

@Composable
internal fun OrderCompleteScreenContent(
    orderNumber: String,
    order: Order? = null,
    onNavigateToOrderDetail: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    val context = LocalContext.current
    var headerHeight by rememberSaveableDp()
    var bottomHeight by rememberSaveableDp()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 가운데보다 조금 위, 위 2 : 아래 3
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .padding(top = headerHeight, bottom = bottomHeight)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(2f))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "주문이 완료되었습니다",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { context.copyTextToClipboard(orderNumber, "주문번호를 복사했습니다") }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "주문번호 $orderNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = "주문번호 복사",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (order != null) {
                Spacer(modifier = Modifier.height(28.dp))
                OrderSummaryCard(order = order)
            }

            Spacer(modifier = Modifier.weight(3f))
        }

        // 주 버튼 주문 상세, 보조 홈으로
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                .onGloballyPositioned { bottomHeight = with(density) { it.size.height.toDp() } },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "주문 상세 보기",
                onClick = onNavigateToOrderDetail
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 결제수단 '카드 삭제'와 같은 회색 투명 버튼
            TextButton(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bounceClick(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("홈으로")
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GlassIconButton(
                    onClick = onNavigateToHome,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "구매완료",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/** 주문 요약 카드: 상품 · 금액 · 적립 · 배송지 */
@Composable
private fun OrderSummaryCard(order: Order) {
    val numberFormat = rememberKoreanNumberFormat()
    val shape = MaterialTheme.shapes.large
    val firstItem = order.items.firstOrNull()
    val creatorNames = order.items.mapNotNull { it.creatorName }.distinct().take(2)
    val earnPoints = order.creatorGroups.sumOf { it.earnPoints }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cardBorder(shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (firstItem != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = firstItem.productImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_image_placeholder),
                    error = painterResource(R.drawable.ic_image_placeholder),
                    fallback = painterResource(R.drawable.ic_image_placeholder)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (creatorNames + "상품 ${order.items.size}개").joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = firstItem.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        SummaryRow(label = "결제 금액") {
            Text(
                text = "${numberFormat.format(order.totalPrice)}원",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // 구매 확정 때 적립, 서버 계산값
        if (earnPoints > 0) {
            SummaryRow(label = "적립 예정") {
                Text(
                    text = "+${numberFormat.format(earnPoints)}P",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        SummaryRow(label = "배송지") {
            Text(
                text = order.shippingAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            value()
        }
    }
}
