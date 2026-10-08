package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ProductDetailTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<String> = listOf("상세 정보")
) {
    val density = LocalDensity.current
    var textWidth by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 탭 아이템
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTab
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(index) }
                    .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 16.dp)
            ) {
                Text(
                    text = tab,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.onSizeChanged { size ->
                        if (textWidth == 0) textWidth = size.width
                    }
                )
            }
        }

        // 구분선 + 라운드 인디케이터
        val indicatorWidth by animateDpAsState(
            targetValue = with(density) { textWidth.toDp() } + 16.dp,
            animationSpec = tween(durationMillis = 250),
            label = "tabIndicatorWidth"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            // 전체 너비 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            // 라운드 인디케이터
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(indicatorWidth)
                    .height(2.dp)
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(1.dp))
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductDetailTabRowPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductDetailTabRow(selectedTab = 0, onTabSelected = {})
                ProductDetailTabRow(
                    selectedTab = 1,
                    onTabSelected = {},
                    tabs = listOf("상세 정보", "리뷰")
                )
            }
        }
    }
}
