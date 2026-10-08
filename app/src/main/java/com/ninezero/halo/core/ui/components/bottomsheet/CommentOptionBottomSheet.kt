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
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 댓글 옵션 시트 */
@Composable
fun CommentOptionBottomSheet(
    isOwnComment: Boolean,
    onDismiss: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
    isDeleted: Boolean = false,
    isBookmarked: Boolean = false,
    onDelete: () -> Unit = {},
    onBookmark: () -> Unit = {},
    onBlock: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState()
) {
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
            when {
                isOwnComment -> {
                    BottomSheetMenuItem(
                        text = "삭제",
                        iconRes = R.drawable.ic_delete,
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        textColor = errorColor,
                        iconTint = errorColor
                    )
                }
                isDeleted -> {
                    BottomSheetMenuItem(
                        text = "차단",
                        iconRes = R.drawable.ic_block,
                        onClick = {
                            onBlock()
                            onDismiss()
                        }
                    )
                }
                else -> {
                    BottomSheetMenuItem(
                        text = if (isBookmarked) "저장 취소" else "저장",
                        iconRes = if (isBookmarked) R.drawable.ic_bookmark_slash else R.drawable.ic_bookmark,
                        onClick = {
                            onBookmark()
                            onDismiss()
                        }
                    )
                    BottomSheetMenuItemGroup(
                        items = listOf(
                            BottomSheetMenuItemData(
                                text = "차단",
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
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentOptionBottomSheetOwnPreview() {
    HaloTheme {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItem(
                text = "삭제",
                iconRes = R.drawable.ic_delete,
                onClick = {},
                textColor = errorColor,
                iconTint = errorColor
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentOptionBottomSheetOtherPreview() {
    HaloTheme {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItem(
                text = "저장",
                iconRes = R.drawable.ic_bookmark,
                onClick = {}
            )
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

@HaloLightDarkPreview
@Composable
private fun CommentOptionBottomSheetDeletedPreview() {
    HaloTheme {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItem(
                text = "차단",
                iconRes = R.drawable.ic_block,
                onClick = {}
            )
        }
    }
}
