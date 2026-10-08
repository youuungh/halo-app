package com.ninezero.halo.feature.settings.presentation.open_source_licenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mikepenz.aboutlibraries.entity.Library
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.settings.presentation.open_source_licenses.components.LibraryItem
import com.ninezero.halo.feature.settings.presentation.open_source_licenses.components.LicenseBottomSheet
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun OpenSourceLicensesScreen(
    onNavigateBack: () -> Unit,
    viewModel: OpenSourceLicensesViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()

    OpenSourceLicensesScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun OpenSourceLicensesScreenContent(
    state: OpenSourceLicensesState,
    onNavigateBack: () -> Unit,
    onIntent: (OpenSourceLicensesIntent) -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }

    var headerHeight by rememberSaveableDp()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            when {
                state.isLoading -> {
                    CenteredLoadingIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight),
                        size = LoadingIndicatorSize.Large
                    )
                }

                state.libraries.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "라이선스 정보를 불러올 수 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = headerHeight + 8.dp, bottom = 24.dp)
                    ) {
                        itemsIndexed(
                            items = state.libraries,
                            key = { _, library -> "${library.uniqueId}:${library.artifactVersion}" }
                        ) { index, library ->
                            Column {
                                LibraryItem(
                                    name = library.name,
                                    meta = libraryMeta(library),
                                    onClick = { onIntent(OpenSourceLicensesIntent.LibraryClick(library)) }
                                )

                                if (index < state.libraries.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 20.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HazeScreenHeader(
            title = "오픈소스 라이선스",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )
    }

    // 라이선스 전문 시트
    val selectedLibrary = state.selectedLibrary
    if (selectedLibrary != null) {
        LicenseBottomSheet(
            library = selectedLibrary,
            onDismiss = { onIntent(OpenSourceLicensesIntent.DismissLicenseSheet) }
        )
    }
}

private fun libraryMeta(library: Library): String? {
    val licenseNames = library.licenses
        .joinToString { it.name }
        .takeIf { it.isNotBlank() }
    return listOfNotNull(library.artifactVersion, licenseNames)
        .joinToString(" · ")
        .takeIf { it.isNotBlank() }
}

@HaloLightDarkPreview
@Composable
private fun OpenSourceLicensesScreenPreview() {
    HaloTheme {
        OpenSourceLicensesScreenContent(
            state = OpenSourceLicensesState(isLoading = false),
            onNavigateBack = {}
        )
    }
}
