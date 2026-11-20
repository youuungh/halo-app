package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.theme.DarkGlassLightTone
import com.ninezero.halo.core.ui.theme.GlassLightTone
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.OpticalSizeValue
import dev.chrisbanes.haze.glass.hazeGlass

/** 앱 테마를 따르는 유리 버튼 배경 */
@Composable
fun Modifier.haloGlass(
    state: HazeState,
    tint: Color,
    shape: RoundedCornerShape,
    backdrop: Color = MaterialTheme.colorScheme.background,
    accent: Boolean = false
): Modifier {
    val isDark = LocalIsDarkTheme.current
    val style = remember(isDark, tint, shape, backdrop) {
        // haze는 시스템 다크 모드만 보므로 앱 테마 값으로 덮음
        GlassStyle.clear.then {
            shape(shape)
            // 투명한 뒤 영역에서 틴트가 사라지지 않게
            backgroundColor(backdrop)
            tint(tint)
            // 빛의 반대편인 아래 테두리가 밝아짐
            lightPosition(if (isDark) DarkLightPosition else Alignment.TopCenter)
            // 안쪽은 서리처럼 흐리고 굴절은 가장자리에 남김
            optics { copy(blurRadius = OpticalSizeValue.Fixed(12.dp)) }
            if (isDark) {
                specularIntensity(0.32f)
                edgeShadow(Color.Transparent)
                ambientResponse(0f)
                contrast(0.12f)
                whitePoint(0.2f)
                chromaMultiplier(1f)
            } else {
                specularIntensity(0.85f)
                edgeShadow(Color.Transparent)
                ambientResponse(0f)
                contrast(0.08f)
                whitePoint(0.17f)
                chromaMultiplier(1.05f)
            }
        }
    }
    val rim = when {
        accent && isDark -> DarkAccentRim
        accent -> LightAccentRim
        isDark -> DarkGlassRim
        else -> LightGlassRim
    }
    // 떠 있는 그림자는 흰 바탕에서 버튼 밑 회색 얼룩으로 보여 뺌
    return dropShadow(shape, rim.outline)
        .dropShadow(shape, rim.leftEdge)
        .dropShadow(shape, rim.rightEdge)
        // 유리가 첫 프레임을 그리기 전에 안쪽 그림자가 비치지 않게
        .background(backdrop, shape)
        .hazeGlass(HazeInput.Sources(state), style)
        // 밝게 더하는 합성은 유리와 같은 캔버스에서 해야 먹음
        .drawBehind {
            val outline = shape.createOutline(size, layoutDirection, this)
            rim.fills.forEach { drawOutline(outline, it.color, blendMode = it.blendMode) }
        }
        .innerShadow(shape, rim.topShade)
        .innerShadow(shape, rim.topLight)
        .innerShadow(shape, rim.bottomLight)
        .clip(shape)
}

private val DarkLightPosition = BiasAlignment(0f, -0.5f)

/** 유리 버튼의 테두리와 볼록한 명암 */
private class GlassRim(
    outlineAlpha: Float,
    edgeAlpha: Float,
    light: Color,
    val fills: List<GlassFill> = emptyList()
) {
    val outline = Shadow(radius = 0.dp, color = Color.Black.copy(alpha = outlineAlpha), spread = 0.5.dp)
    val leftEdge = edge(-1.25.dp, edgeAlpha)
    val rightEdge = edge(1.25.dp, edgeAlpha)
    val topShade = Shadow(
        radius = 30.dp,
        color = Color.Black.copy(alpha = 0.1f),
        spread = (-40).dp,
        offset = DpOffset(0.dp, 40.dp)
    )
    val topLight = glassEdgeLight(40.dp, light)
    val bottomLight = glassEdgeLight((-40).dp, light)

    private fun edge(x: Dp, alpha: Float) =
        Shadow(radius = 0.dp, color = Color.Black.copy(alpha = alpha), spread = (-0.75).dp, offset = DpOffset(x, 0.dp))
}

/** 유리 위아래 안쪽에 맺히는 빛, 메뉴도 같이 씀 */
internal fun glassEdgeLight(y: Dp, color: Color) =
    Shadow(radius = 10.dp, color = color, spread = (-40).dp, offset = DpOffset(0.dp, y), blendMode = BlendMode.Plus)

private class GlassFill(val color: Color, val blendMode: BlendMode = BlendMode.SrcOver)

// 뒤 내용을 우유빛으로 덮는 키트의 면 색
private val LightGlassFills = listOf(
    GlassFill(Color.Black.copy(alpha = 0.25f)),
    GlassFill(Color.White.copy(alpha = 0.25f)),
    GlassFill(Color(0xFF444444).copy(alpha = 0.6f), BlendMode.Plus),
    GlassFill(Color(0xFFF8F8F8).copy(alpha = 0.2f), BlendMode.Luminosity)
)
private val DarkGlassFills = listOf(GlassFill(Color(0xFF999999).copy(alpha = 0.17f), BlendMode.Luminosity))

private val LightGlassRim = GlassRim(0.09f, 0.18f, GlassLightTone, LightGlassFills)
private val DarkGlassRim = GlassRim(0f, 0.1f, DarkGlassLightTone, DarkGlassFills)

// 강조 버튼은 테두리 선 없이 좌우만 연하게
private val LightAccentRim = GlassRim(0f, 0.1f, GlassLightTone)
private val DarkAccentRim = GlassRim(0f, 0.1f, DarkGlassLightTone)
