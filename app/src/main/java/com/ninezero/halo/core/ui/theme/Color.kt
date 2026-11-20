package com.ninezero.halo.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 주 색
val Primary = Color(0xFF007AFF)
val PrimaryVariant = Color(0xFF0A84FF)
val OnPrimary = Color.White

// 보조
val Secondary = Color(0xFFFF2D55)
val SecondaryVariant = Color(0xFFFF375F)
val OnSecondary = Color.White

// 배경 · 표면
val Background = Color.White
val Surface = Color.White
val SurfaceVariant = Color(0xFFF5F5F5)
val SurfaceContainer = Color(0xFFFAFAFA)
val OnBackground = Color(0xFF1C1C1E)
val OnSurface = Color(0xFF1C1C1E)
val OnSurfaceVariant = Color(0xFF767676)

// 오류
val Error = Color(0xFFFF3B30)
val OnError = Color.White

// 다크 배경 · 표면
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2D2D2D)
val DarkSurfaceContainer = Color(0xFF252525)
val DarkOnBackground = Color(0xFFE1E1E1)
val DarkOnSurface = Color(0xFFE1E1E1)
val DarkOnSurfaceVariant = Color(0xFF8A8A8A)

// 외곽선 · 구분선
val Outline = Color(0xFFE0E0E0)
val OutlineVariant = Color(0xFFBDBDBD)
val DarkOutline = Color(0xFF3D3D3D)
val DarkOutlineVariant = Color(0xFF4D4D4D)

// 반전 표면
val InverseSurface = Color(0xFF1C1C1E)
val OnInverseSurface = Color.White
val DarkInverseSurface = Color(0xFFE1E1E1)
val DarkOnInverseSurface = Color(0xFF1E1E1E)

// 성공 · 경고
val Success = Color(0xFF34C759)
val Warning = Color(0xFFFF9500)

// 채팅 하이라이트
val SearchMatchYellow = Color(0xFFFFEB3B)

// 정보
val Info = Color(0xFF5AC8FA)

// 좋아요
val Like = Color(0xFFFF6B6B)

// 로그인 브랜드
val KakaoYellow = Color(0xFFFEE500)
val NaverGreen = Color(0xFF03C75A)
val SocialIconDark = Color(0xFF191919)

// 별점
val StarYellow = Color(0xFFFFC107)

// 품절 배지
val SoldOutBgLight = Color(0xFFFEF2F2)
val SoldOutTextLight = Color(0xFFDC2626)
val SoldOutBgDark = Color(0xFF3D1515)
val SoldOutTextDark = Color(0xFFFF6B6B)

// 멤버십
val MembershipLabelLight = Color(0xFF4F6BE0)
val MembershipLabelDark = Color(0xFF9BB4FF)
val MembershipBorderLight = Color(0xFF6B7CE6)
val MembershipTextStart = Color(0xFF5B5FE0)
val MembershipTextEnd = Color(0xFF2F8FE8)
val MembershipSparkle = Color(0xFFB6C4FF)

// 유리 안쪽 빛, 버튼 · 팝업 공용
val GlassLightTone = Color(0xFF282828)
val DarkGlassLightTone = Color(0xFF1A1A1A)

// 다크 유리 바탕
val DarkGlassTint = Color(0xFF28282C)

// 강조 유리 버튼
val GlassAccentBlue = Color(0xFF0088FF)
val PagerGlassTint = Color(0xFF1E1E24)

// 다크 메뉴 칸
val DarkMenuTile = Color(0xFF1C1C1E)
val DarkExpandText = Color(0xFF4DA3FF)

// 사진 없는 프로필 회색
val FallbackCoverGray = Color(0xFF6B6B70)

// Halo 시그니처 그라데이션
val HaloGradientStart = Color(0xFF9B9FFF)
val HaloGradientMid1 = Color(0xFF6BB6FF)
val HaloGradientMid2 = Color(0xFF7DD9FF)
val HaloGradientEnd = Color(0xFFA5E5FF)

val HaloGradient: Brush = Brush.horizontalGradient(
    colorStops = arrayOf(
        0.0f to HaloGradientStart,
        0.4f to HaloGradientMid1,
        0.75f to HaloGradientMid2,
        1.0f to HaloGradientEnd
    )
)
