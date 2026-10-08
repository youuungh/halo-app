package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloPreview

enum class ChatOptionMode {
    ARCHIVE,
    UNARCHIVE
}

/** 채팅 옵션 시트 */
@Composable
fun ChatOptionsBottomSheet(
    sheetState: SheetState,
    mode: ChatOptionMode,
    onActionClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = modifier
    ) {
        BottomSheetDragHandle()
        Column(modifier = Modifier.navigationBarsPadding()) {
            BottomSheetMenuItemGroup(items = chatOptionItems(mode, onActionClick, onDeleteClick))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun chatOptionItems(
    mode: ChatOptionMode,
    onActionClick: () -> Unit,
    onDeleteClick: () -> Unit
): List<BottomSheetMenuItemData> {
    val (iconRes, actionText) = when (mode) {
        ChatOptionMode.ARCHIVE -> R.drawable.ic_archive_plus to "보관"
        ChatOptionMode.UNARCHIVE -> R.drawable.ic_archive_minus to "보관 해제"
    }
    return listOf(
        BottomSheetMenuItemData(
            text = actionText,
            iconRes = iconRes,
            onClick = onActionClick
        ),
        BottomSheetMenuItemData(
            text = "대화 삭제",
            iconRes = R.drawable.ic_delete,
            onClick = onDeleteClick,
            textColor = MaterialTheme.colorScheme.error,
            iconTint = MaterialTheme.colorScheme.error
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun ChatOptionsBottomSheetArchivePreview() {
    HaloPreview {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                BottomSheetMenuItemGroup(items = chatOptionItems(ChatOptionMode.ARCHIVE, {}, {}))
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatOptionsBottomSheetUnarchivePreview() {
    HaloPreview {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                BottomSheetMenuItemGroup(items = chatOptionItems(ChatOptionMode.UNARCHIVE, {}, {}))
            }
        }
    }
}
