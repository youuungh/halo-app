package com.ninezero.halo.feature.explore.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.ninezero.halo.core.ui.components.media.LocalAutoplayEnabled
import com.ninezero.halo.feature.explore.domain.model.Banner
import com.ninezero.halo.ui.theme.HaloTheme
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun BannerSection(
    banners: List<Banner>,
    isLoading: Boolean,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            BannerSkeleton(modifier = modifier)
        }

        banners.isEmpty() -> {
            // 배너 없으면 영역도 없음
        }

        else -> {
            // 2개 이상일 때 무한 루프
            val isInfinite = banners.size > 1
            val pagerState = rememberPagerState(
                initialPage = if (isInfinite) banners.size * 1000 else 0,
                pageCount = { if (isInfinite) Int.MAX_VALUE else banners.size }
            )

            // 자동 슬라이드, 터치 중 정지·뗀 뒤 4초
            var isPressed by remember { mutableStateOf(false) }
            val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
            // 숨겨진 상태에선 루프 정지 → 복귀 시 자동 재시작
            val autoplayEnabled = LocalAutoplayEnabled.current
            LaunchedEffect(isDragged, isPressed, autoplayEnabled) {
                if (!isInfinite || isDragged || isPressed || !autoplayEnabled) return@LaunchedEffect

                // 애니메이션이 끊긴 채 남은 상태면 현재 페이지로 정착
                if (!pagerState.isScrollInProgress && pagerState.currentPageOffsetFraction != 0f) {
                    pagerState.animateScrollToPage(pagerState.currentPage)
                }

                while (true) {
                    delay(4000)
                    if (!pagerState.isScrollInProgress) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            }

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    // 배너 영역 press 감시 → 자동 슬라이드 정지
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                isPressed = event.changes.any { it.pressed }
                            }
                        }
                    }
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    pageSpacing = 8.dp
                ) { page ->
                    val realPage = if (isInfinite) page % banners.size else page
                    val isCurrentPage by remember(page) {
                        derivedStateOf {
                            pagerState.currentPage == page &&
                                pagerState.currentPageOffsetFraction.absoluteValue < 0.5f
                        }
                    }

                    BannerItem(
                        banner = banners[realPage],
                        onClick = { onBannerClick(banners[realPage]) },
                        alpha = {
                            val pageOffset =
                                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                            )
                        },
                        isCurrentPage = isCurrentPage
                    )
                }

                // 페이지 인디케이터
                if (banners.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 20.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = " ${(pagerState.currentPage % banners.size) + 1}/${banners.size} ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerItem(
    banner: Banner,
    onClick: () -> Unit,
    alpha: () -> Float,
    isCurrentPage: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color.Black) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha() }
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(banner.imageUrl)
                .allowHardware(false)
                .build(),
            contentDescription = banner.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onState = { state ->
                if (state is AsyncImagePainter.State.Success) {
                    val bitmap = state.result.image.toBitmap()
                    bitmap.let {
                        Palette.from(it).generate { palette ->
                            palette?.let { p ->
                                val color = p.getDarkMutedColor(
                                    p.getDominantColor(android.graphics.Color.BLACK)
                                )
                                dominantColor = Color(color)
                            }
                        }
                    }
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            dominantColor.copy(alpha = 0.8f)
                        ),
                        startY = 300f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = isCurrentPage,
                enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it / 2 }
                    ),
                exit = fadeOut(animationSpec = tween(300)) +
                    slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { it / 2 }
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (banner.description != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = banner.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BannerSectionPreview() {
    val sampleBanners = listOf(
        Banner(
            imageUrl = "https://picsum.photos/400/400",
            title = "신규 크리에이터 소개",
            description = "이번 주 주목할 만한 크리에이터를 만나보세요",
            link = "/creator/1"
        ),
        Banner(
            imageUrl = "https://picsum.photos/400/400",
            title = "인기 상품 모음",
            description = "지금 가장 핫한 상품들을 확인하세요",
            link = "/product/featured"
        )
    )
    HaloTheme {
        BannerSection(
            banners = sampleBanners,
            isLoading = false,
            onBannerClick = {}
        )
    }
}
