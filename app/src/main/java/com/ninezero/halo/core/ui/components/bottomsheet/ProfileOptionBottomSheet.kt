package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.copyLinkToClipboard
import com.ninezero.halo.core.deeplink.shareTextExternally
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 프로필 옵션 시트 */
@Composable
fun ProfileOptionBottomSheet(
    onDismiss: () -> Unit,
    onBlock: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
    isBlocked: Boolean = false,
    profileLink: String? = null,  // 외부 링크, null이면 숨김
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val context = LocalContext.current
    val errorColor = MaterialTheme.colorScheme.error

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            BottomSheetDragHandle()
            if (profileLink != null) {
                BottomSheetMenuItemGroup(
                    items = listOf(
                        BottomSheetMenuItemData(
                            text = "링크 복사",
                            iconRes = R.drawable.ic_link,
                            onClick = {
                                context.copyLinkToClipboard(profileLink)
                                onDismiss()
                            }
                        ),
                        BottomSheetMenuItemData(
                            text = "프로필 공유",
                            iconRes = R.drawable.ic_share,
                            onClick = {
                                context.shareTextExternally(profileLink)
                                onDismiss()
                            }
                        )
                    )
                )
            }
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(
                        text = if (isBlocked) "차단 해제" else "차단",
                        iconRes = R.drawable.ic_block,
                        onClick = {
                            onBlock()
                            onDismiss()
                        }
                    ),
                    BottomSheetMenuItemData(
                        text = "신고",
                        iconRes = R.drawable.ic_warning,
                        onClick = {
                            onReport()
                            onDismiss()
                        },
                        textColor = errorColor,
                        iconTint = errorColor
                    )
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProfileOptionBottomSheetPreview() {
    HaloTheme {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(
                        text = "차단",
                        iconRes = R.drawable.ic_block,
                        onClick = {}
                    ),
                    BottomSheetMenuItemData(
                        text = "신고",
                        iconRes = R.drawable.ic_warning,
                        onClick = {},
                        textColor = errorColor,
                        iconTint = errorColor
                    )
                )
            )
        }
    }
}
