package com.ninezero.halo.feature.settings.presentation.open_source_licenses.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.mikepenz.aboutlibraries.entity.Library
import com.ninezero.halo.core.ui.components.bottomsheet.SheetHeader
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

@Composable
fun LicenseBottomSheet(
    library: Library,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }

    val statusBarPx = WindowInsets.statusBars.getTop(density)
    val statusBarDp = with(density) { statusBarPx.toDp() }
    // 상단이 이 거리 안이면 모프 시작, 0px에서 완성
    val morphRangePx = statusBarPx + with(density) { 56.dp.toPx() }
    val morphProgress by remember(morphRangePx) {
        derivedStateOf {
            val offset = runCatching { sheetState.requireOffset() }.getOrNull() ?: Float.MAX_VALUE
            (1f - offset / morphRangePx).coerceIn(0f, 1f)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceColor,
        // 도킹에 가까울수록 코너를 폄
        shape = RoundedCornerShape(
            topStart = lerp(28.dp, 0.dp, morphProgress),
            topEnd = lerp(28.dp, 0.dp, morphProgress)
        ),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        // 펼친 상태는 뒤로가기 시 바로 접음
        BackHandler(
            enabled = sheetState.currentValue == SheetValue.Expanded && sheetState.hasPartiallyExpandedState
        ) {
            scope.launch { sheetState.partialExpand() }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()  // 위로 끌면 최상단까지 확장
                    .hazeSource(hazeState)
                    .blockSheetFling()
                    .verticalScroll(rememberScrollState(), overscrollEffect = null)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(headerHeight + 8.dp))

                if (library.licenses.isEmpty()) {
                    Text(
                        text = "라이선스 정보가 없어요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    library.licenses.forEachIndexed { index, license ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        Text(
                            text = license.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = license.licenseContent?.takeIf { it.isNotBlank() }
                                ?: license.url
                                ?: "라이선스 전문이 제공되지 않습니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }

            SheetHeader(
                title = library.name,
                onClose = onDismiss,
                hazeState = hazeState,
                onHeightChange = { headerHeight = it },
                subtitle = library.artifactVersion,
                topInset = statusBarDp * morphProgress,
                handleAlpha = 1f - morphProgress
            )
        }
    }
}
