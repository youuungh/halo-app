package com.ninezero.halo.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF007AFF)
val PrimaryVariant = Color(0xFF0A84FF)
val OnPrimary = Color.White

// 보조 색상
val Secondary = Color(0xFFFF2D55)
val SecondaryVariant = Color(0xFFFF375F)
val OnSecondary = Color.White

// 배경·표면
val Background = Color.White
val Surface = Color.White
val SurfaceVariant = Color(0xFFF5F5F5)
val SurfaceContainer = Color(0xFFFAFAFA)
val OnBackground = Color(0xFF1C1C1E)
val OnSurface = Color(0xFF1C1C1E)
val OnSurfaceVariant = Color(0xFF9E9E9E)

// 에러
val Error = Color(0xFFFF3B30)
val OnError = Color.White

// 다크 테마 색상
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2D2D2D)
val DarkSurfaceContainer = Color(0xFF252525)
val DarkOnBackground = Color(0xFFE1E1E1)
val DarkOnSurface = Color(0xFFE1E1E1)
val DarkOnSurfaceVariant = Color(0xFF8A8A8A)

// 외곽선·구분선
val Outline = Color(0xFFE0E0E0)
val OutlineVariant = Color(0xFFBDBDBD)
val DarkOutline = Color(0xFF3D3D3D)
val DarkOutlineVariant = Color(0xFF4D4D4D)

// 반전
val InverseSurface = Color(0xFF1C1C1E)
val OnInverseSurface = Color.White
val DarkInverseSurface = Color(0xFFE1E1E1)
val DarkOnInverseSurface = Color(0xFF1E1E1E)

// 추가 시맨틱 색상
val Success = Color(0xFF34C759)
val Warning = Color(0xFFFF9500)
val Info = Color(0xFF5AC8FA)

// 소셜 색상
val Like = Color(0xFFFF6B6B)

// 브랜드 색, 테마 무관
val KakaoYellow = Color(0xFFFEE500)
val NaverGreen = Color(0xFF03C75A)

// 배지 색상
val SoldOutBgLight = Color(0xFFFEF2F2)
val SoldOutTextLight = Color(0xFFDC2626)
val SoldOutBgDark = Color(0xFF3D1515)
val SoldOutTextDark = Color(0xFFFF6B6B)

// Halo 시그니처 그라데이션
val HaloGradientStart = Color(0xFF9B9FFF)
val HaloGradientMid1 = Color(0xFF6BB6FF)
val HaloGradientMid2 = Color(0xFF7DD9FF)
val HaloGradientEnd = Color(0xFFA5E5FF)

val HaloGradientColors = listOf(
    HaloGradientStart,
    HaloGradientMid1,
    HaloGradientMid2,
    HaloGradientEnd
)

val HaloGradient: Brush = Brush.horizontalGradient(
    colorStops = arrayOf(
        0.0f to HaloGradientStart,
        0.4f to HaloGradientMid1,
        0.75f to HaloGradientMid2,
        1.0f to HaloGradientEnd
    )
)
