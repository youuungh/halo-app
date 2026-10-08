package com.ninezero.halo.feature.share.presentation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ShareSheetHost(
    visible: Boolean,
    shareAction: (ShareViewModel) -> Unit,
    onDismiss: () -> Unit,
    onShareSuccess: (sharedRoomIds: List<Int>, recipientCount: Int) -> Unit,
    externalLink: String? = null
) {
    if (visible) {
        val shareViewModel: ShareViewModel = hiltViewModel()
        ShareBottomSheet(
            viewModel = shareViewModel,
            onDismiss = {
                shareViewModel.onIntent(ShareIntent.Reset)
                onDismiss()
            },
            onShareSuccess = { result ->
                shareViewModel.onIntent(ShareIntent.Reset)
                onShareSuccess(result.sharedRoomIds, result.recipientCount)
            },
            shareAction = shareAction,
            externalLink = externalLink
        )
    }
}
