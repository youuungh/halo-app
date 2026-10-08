package com.ninezero.halo.core.ui.components.dialog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 로딩 다이얼로그 */
@Composable
fun LoadingDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        CenteredLoadingIndicator(
            modifier = Modifier.fillMaxSize(),
            size = LoadingIndicatorSize.Large
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun LoadingDialogPreview() {
    HaloTheme {
        LoadingDialog()
    }
}
