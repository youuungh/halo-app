package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.PagerGlassTint
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.graphics.shadow.Shadow as DropShadow

internal val ProductHeroHeight = 400.dp
internal val ProductHeroOverlap = 44.dp

private val HeroFadeHeight = 120.dp
private val ProductFadeEasing = CubicBezierEasing(0.45f, 0f, 0.75f, 0.85f)
private val PageCountBottomPadding = 64.dp
private val HeroTopBlurHeight = 56.dp
private val HeroTopShadeHeight = 130.dp
private const val HERO_TOP_SHADE_ALPHA = 0.46f
private const val LOOP_PAGE_COUNT = 100_000
private const val PAGE_COUNT_TINT_ALPHA = 0.46f
private const val PAGE_COUNT_TOTAL_ALPHA = 0.72f

@Composable
fun ProductImagePager(
    imageUrls: List<String>,
    onImageClick: (index: Int) -> Unit,
    parallaxOffset: () -> Float,
    modifier: Modifier = Modifier
) {
    if (imageUrls.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(ProductHeroHeight)
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

    val imageCount = imageUrls.size
    val isLooping = imageCount > 1
    val pagerState = rememberPagerState(
        initialPage = if (isLooping) LOOP_PAGE_COUNT / 2 - (LOOP_PAGE_COUNT / 2) % imageCount else 0,
        pageCount = { if (isLooping) LOOP_PAGE_COUNT else imageCount }
    )
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ProductHeroHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .hazeSource(hazeState)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = parallaxOffset() }
            ) { page ->
                val imageIndex = page % imageCount
                AsyncImage(
                    model = imageUrls[imageIndex],
                    contentDescription = "상품 이미지 ${imageIndex + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onImageClick(imageIndex) },
                    contentScale = ContentScale.Crop
                )
            }

            // 상태바·상단 버튼 뒤를 어둡게
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(HeroTopShadeHeight)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = HERO_TOP_SHADE_ALPHA),
                            0.5f to Color.Black.copy(alpha = HERO_TOP_SHADE_ALPHA * 0.4f),
                            1f to Color.Transparent
                        )
                    )
            )
        }

        // 다른 화면 톱바와 같은 블러
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .haloBlur(
                    hazeState,
                    HaloBlurStyle(
                        backgroundColor = backgroundColor,  // 사진이 뜨기 전 검게 비치지 않게
                        tint = Color.Transparent,
                        noiseFactor = 0f,
                        progressive = HazeProgressive.verticalGradient(startIntensity = 0.7f, endIntensity = 0f)
                    )
                )
                .statusBarsPaddingIgnoringVisibility()
                .height(HeroTopBlurHeight)
        )

        // 사진 끝을 바탕색으로 녹임
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(HeroFadeHeight)
                .haloBlur(hazeState, HaloBlurs.fadeUp(backgroundColor, end = 1f, easing = ProductFadeEasing))
        )

        if (isLooping) {
            PageCountPill(
                pagerState = pagerState,
                imageCount = imageCount,
                hazeState = hazeState,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = PageCountBottomPadding)
            )
        }
    }
}

@Composable
private fun PageCountPill(
    pagerState: PagerState,
    imageCount: Int,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val glassTint = PagerGlassTint
    val density = LocalDensity.current
    val textStyle = MaterialTheme.typography.labelMedium.copy(
        fontSize = 12.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.sp,
        shadow = with(density) {
            Shadow(color = Color.Black.copy(alpha = 0.25f), offset = Offset(0f, 1.dp.toPx()), blurRadius = 2.dp.toPx())
        }
    )

    Row(
        modifier = modifier
            .dropShadow(
                CircleShape,
                DropShadow(radius = 8.dp, color = Color.Black.copy(alpha = 0.15f), offset = DpOffset(0.dp, 2.dp))
            )
            .clip(CircleShape)
            .haloBlur(
                hazeState,
                HaloBlurStyle(
                    backgroundColor = MaterialTheme.colorScheme.background,  // 사진이 뜨기 전엔 이 색이 비침
                    tint = glassTint.copy(alpha = PAGE_COUNT_TINT_ALPHA),
                    fallbackTint = glassTint.copy(alpha = 0.6f),
                    blurRadius = 16.dp,
                    noiseFactor = 0f
                )
            )
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.06f))))
            .border(
                0.5.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.15f))),
                CircleShape
            )
            // 높이 22dp, 글자 12 + 위아래 5
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "${pagerState.currentPage % imageCount + 1}",
            style = textStyle,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(10.dp)
                .background(Color.White.copy(alpha = 0.5f))
        )
        Text(
            text = "$imageCount",
            style = textStyle,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = PAGE_COUNT_TOTAL_ALPHA)
        )
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
                    parallaxOffset = { 0f }
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
                ProductImagePager(imageUrls = emptyList(), onImageClick = {}, parallaxOffset = { 0f })
            }
        }
    }
}
