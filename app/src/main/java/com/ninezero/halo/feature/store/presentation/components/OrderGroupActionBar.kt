package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

/** 그룹 아래 글자 버튼 하나 */
@Immutable
data class OrderGroupAction(
    val label: String,
    val color: Color,
    val badge: String? = null,
    val isLoading: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

/** 그룹 맨 아래 가는 선 + 글자 버튼 줄. 둘이면 반씩 나눔 */
@Composable
fun OrderGroupActionBar(actions: List<OrderGroupAction>) {
    if (actions.isEmpty()) return
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    HorizontalDivider(thickness = 0.5.dp, color = lineColor)
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        actions.forEachIndexed { index, action ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .background(lineColor)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clickable(enabled = action.enabled && !action.isLoading, onClick = action.onClick),
                contentAlignment = Alignment.Center
            ) {
                if (action.isLoading) {
                    // 칸 크기는 그대로, 글자 자리에 로딩
                    LoadingIndicator(size = LoadingIndicatorSize.Small)
                } else {
                    Row(
                        modifier = Modifier.alpha(if (action.enabled) 1f else 0.4f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = action.color
                        )
                        if (action.badge != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = action.badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = action.color,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(action.color.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderGroupActionBarPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderGroupActionBar(
                    listOf(
                        OrderGroupAction("배송조회", MaterialTheme.colorScheme.onSurface) {},
                        OrderGroupAction("리뷰 쓰기", MaterialTheme.colorScheme.primary, badge = "D-10") {}
                    )
                )
                Row(modifier = Modifier.padding(16.dp)) { CartCircleButton {} }
            }
        }
    }
}
