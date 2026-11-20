package com.ninezero.halo.core.ui.components.selector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.shadow.Shadow as DropShadow

/** 탐색 · 프로필 · 팔로워 알약 탭 공통 값 */
object CapsuleTabDefaults {
    val Height = 40.dp

    // 다크는 표면색이면 유리보다 어두워 파여 보임
    val DarkIndicatorColor = Color.White.copy(alpha = 0.12f)

    // 밝은 블러 위에서 알약 경계가 묻히지 않게 살짝 띄움
    val IndicatorShadow = DropShadow(radius = 6.dp, color = Color.Black.copy(alpha = 0.1f), offset = DpOffset(0.dp, 2.dp))
}
