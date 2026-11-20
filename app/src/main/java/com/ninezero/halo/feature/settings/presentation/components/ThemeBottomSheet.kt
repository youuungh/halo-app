package com.ninezero.halo.feature.settings.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.datastore.ThemeMode
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.SheetOptionItem
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

/** 테마 선택 시트 */
@Composable
fun ThemeBottomSheet(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val contentColor = MaterialTheme.colorScheme.surfaceVariant

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = contentColor
            ) {
                Column {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SheetOptionItem(
                            text = mode.sheetLabel,
                            isSelected = mode == currentMode,
                            onClick = {
                                onModeChange(mode)
                                onDismiss()
                            }
                        )
                        if (index < ThemeMode.entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ThemeBottomSheetContentPreview() {
    HaloTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            SheetOptionItem(
                                text = mode.sheetLabel,
                                isSelected = mode == ThemeMode.SYSTEM,
                                onClick = {}
                            )
                            if (index < ThemeMode.entries.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
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
}
