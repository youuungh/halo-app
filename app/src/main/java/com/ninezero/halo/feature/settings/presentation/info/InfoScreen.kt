package com.ninezero.halo.feature.settings.presentation.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.launchCustomTab
import com.ninezero.halo.feature.settings.presentation.components.SettingsMenuItem
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

private const val DEVELOPER_GITHUB_URL = "https://github.com/youuungh"

@Composable
fun InfoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOpenSourceLicenses: () -> Unit = {}
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }

    var headerHeight by rememberSaveableDp()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 16.dp))

            SettingsMenuItem(
                iconRes = null,
                title = "개발자 정보",
                onClick = { context.launchCustomTab(DEVELOPER_GITHUB_URL) }
            )

            SettingsMenuItem(
                iconRes = null,
                title = "오픈소스 라이선스",
                onClick = onNavigateToOpenSourceLicenses
            )
        }

        HazeScreenHeader(
            title = "정보",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun InfoScreenPreview() {
    HaloTheme {
        InfoScreen(
            onNavigateBack = {},
            onNavigateToOpenSourceLicenses = {}
        )
    }
}
