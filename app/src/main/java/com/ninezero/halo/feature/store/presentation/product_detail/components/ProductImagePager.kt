package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun ProductImagePager(
    imageUrls: List<String>,
    onImageClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewCount: Int = 0,
    likeCount: Int = 0
) {
    if (imageUrls.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "이미지 없음",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })
    val hazeState = remember { HazeState() }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .hazeSource(hazeState)
        ) { page ->
            AsyncImage(
                model = imageUrls[page],
                contentDescription = "상품 이미지 ${page + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onImageClick(page) },
                contentScale = ContentScale.Crop
            )
        }

        // 조회 수 + 좋아요 수
        val glassBackground = MaterialTheme.colorScheme.surface
        val pillContentColor = Color.White.copy(alpha = 0.9f)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 12.dp,
                    bottom = if (imageUrls.size > 1) 25.dp else 12.dp
                )
                .clip(RoundedCornerShape(12.dp))
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = glassBackground.copy(alpha = 0.7f),
                        tint = HaloBlurTint(glassBackground.copy(alpha = 0.4f)),
                        blurRadius = 20.dp,
                        noiseFactor = 0f
                    )
                }
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val numberFormat = rememberKoreanNumberFormat()
            val pillTextStyle = MaterialTheme.typography.labelSmall.copy(
                lineHeight = MaterialTheme.typography.labelSmall.fontSize
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_eye),
                    contentDescription = "조회수",
                    modifier = Modifier.size(12.dp),
                    tint = pillContentColor
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = numberFormat.format(viewCount),
                    style = pillTextStyle,
                    color = pillContentColor
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_heart),
                    contentDescription = "좋아요",
                    modifier = Modifier.size(12.dp),
                    tint = pillContentColor
                )
                Spacer(modifier = Modifier.width(3.dp))
                AnimatedCounter(
                    count = likeCount,
                    style = pillTextStyle,
                    color = pillContentColor
                )
            }
        }

        // Page indicator
        if (imageUrls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(imageUrls.size) { index ->
                    // 사진 위 오버레이라 테마 무관 흰색
                    val segmentColor by animateColorAsState(
                        targetValue = Color.White.copy(
                            alpha = if (index == pagerState.currentPage) 0.9f else 0.35f
                        ),
                        label = "pageSegmentColor"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(segmentColor)
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductImagePagerPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductImagePager(
                    imageUrls = List(3) { "https://picsum.photos/400/400?random=$it" },
                    onImageClick = {},
                    viewCount = 1234,
                    likeCount = 89
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductImagePagerEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductImagePager(imageUrls = emptyList(), onImageClick = {})
            }
        }
    }
}
