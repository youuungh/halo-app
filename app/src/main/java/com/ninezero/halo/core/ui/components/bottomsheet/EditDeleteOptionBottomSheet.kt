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
import com.ninezero.halo.core.deeplink.copyCodeToClipboard
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 수정·삭제 옵션 시트 */
@Composable
fun EditDeleteOptionBottomSheet(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    isActive: Boolean = false,
    onToggleActive: (() -> Unit)? = null,
    copyCode: String? = null
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
        BottomSheetDragHandle()
        Column(modifier = Modifier.navigationBarsPadding()) {
            if (copyCode != null) {
                BottomSheetMenuItem(
                    text = "코드 복사",
                    iconRes = R.drawable.ic_copy,
                    onClick = {
                        context.copyCodeToClipboard(copyCode)
                        onDismiss()
                    }
                )
            }
            if (onToggleActive != null) {
                BottomSheetMenuItemGroup(
                    items = listOf(
                        BottomSheetMenuItemData(
                            text = if (isActive) "비활성화" else "활성화",
                            iconRes = if (isActive) R.drawable.ic_pause else R.drawable.ic_play,
                            onClick = {
                                onToggleActive()
                                onDismiss()
                            }
                        )
                    )
                )
            }
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(
                        text = "수정",
                        iconRes = R.drawable.ic_edit,
                        onClick = {
                            onEdit()
                            onDismiss()
                        }
                    ),
                    BottomSheetMenuItemData(
                        text = "삭제",
                        iconRes = R.drawable.ic_delete,
                        onClick = {
                            onDelete()
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
private fun EditDeleteOptionBottomSheetPreview() {
    HaloTheme {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(
                        text = "수정",
                        iconRes = R.drawable.ic_edit,
                        onClick = {}
                    ),
                    BottomSheetMenuItemData(
                        text = "삭제",
                        iconRes = R.drawable.ic_delete,
                        onClick = {},
                        textColor = errorColor,
                        iconTint = errorColor
                    )
                )
            )
        }
    }
}
