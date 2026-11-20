package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaType
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

/** 뷰어 옵션 시트 */
@Composable
fun ViewerBottomSheet(
    mediaType: ViewerMediaType,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val saveText = when (mediaType) {
        ViewerMediaType.IMAGE -> "이미지 저장"
        ViewerMediaType.VIDEO -> "동영상 저장"
    }

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column {
            BottomSheetMenuItem(
                text = saveText,
                iconRes = R.drawable.ic_arrow_down_circle,
                onClick = {
                    onSave()
                    onDismiss()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ViewerBottomSheetImagePreview() {
    HaloTheme {
        Column {
            BottomSheetMenuItem(
                text = "이미지 저장",
                iconRes = R.drawable.ic_arrow_down_circle,
                onClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ViewerBottomSheetVideoPreview() {
    HaloTheme {
        Column {
            BottomSheetMenuItem(
                text = "동영상 저장",
                iconRes = R.drawable.ic_arrow_down_circle,
                onClick = {}
            )
        }
    }
}
