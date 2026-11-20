package com.ninezero.halo.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object MembershipColors {
    private val TextGradientLight = Brush.horizontalGradient(listOf(MembershipTextStart, MembershipTextEnd))
    private val CardBorderLight = MembershipBorderLight.copy(alpha = 0.2f)
    private val CardBorderDark = MembershipLabelDark.copy(alpha = 0.18f)

    val label: Color
        @Composable @ReadOnlyComposable
        get() = if (LocalIsDarkTheme.current) MembershipLabelDark else MembershipLabelLight

    val textBrush: Brush
        @Composable @ReadOnlyComposable
        get() = if (LocalIsDarkTheme.current) HaloGradient else TextGradientLight

    // 배지는 작아서 카드보다 진하게
    val badgeBrush: Brush
        @Composable @ReadOnlyComposable
        get() {
            val isDark = LocalIsDarkTheme.current
            return Brush.horizontalGradient(
                listOf(
                    HaloGradientStart.copy(alpha = if (isDark) 0.3f else 0.22f),
                    HaloGradientMid1.copy(alpha = if (isDark) 0.22f else 0.16f)
                )
            )
        }

    val cardBrush: Brush
        @Composable @ReadOnlyComposable
        get() = gradient(if (LocalIsDarkTheme.current) listOf(0.2f, 0.16f, 0.16f) else listOf(0.16f, 0.14f, 0.14f))

    // 묶음 카드는 면적이 넓어 요약 카드보다 옅게
    val groupCardBrush: Brush
        @Composable @ReadOnlyComposable
        get() = gradient(if (LocalIsDarkTheme.current) listOf(0.15f, 0.12f, 0.12f) else listOf(0.11f, 0.09f, 0.09f))

    val groupCardBorder: Color
        @Composable @ReadOnlyComposable
        get() = if (LocalIsDarkTheme.current) CardBorderDark else CardBorderLight

    private fun gradient(alphas: List<Float>) = Brush.horizontalGradient(
        listOf(
            HaloGradientStart.copy(alpha = alphas[0]),
            HaloGradientMid1.copy(alpha = alphas[1]),
            HaloGradientMid2.copy(alpha = alphas[2])
        )
    )
}
