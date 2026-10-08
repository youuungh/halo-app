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
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloPreview

/** 포스트 옵션 시트 */
@Composable
fun PostOptionBottomSheet(
    isOwnPost: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBookmark: () -> Unit,
    onNotInterested: () -> Unit,
    onBlock: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
    isBookmarked: Boolean = false,
    canPin: Boolean = false,  // 고정 가능 여부는 호출부가 글 종류·권한으로 판단
    isPinned: Boolean = false,
    canCreatorDelete: Boolean = false,
    copyLink: String? = null,  // 외부 링크, null이면 숨김
    onPin: () -> Unit = {},
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
        BottomSheetDragHandle()
        Column(modifier = Modifier.navigationBarsPadding()) {
            if (copyLink != null) {
                BottomSheetMenuItem(
                    text = "링크 복사",
                    iconRes = R.drawable.ic_link,
                    onClick = {
                        context.copyLinkToClipboard(copyLink)
                        onDismiss()
                    }
                )
            }
            if (canPin) {
                BottomSheetMenuItem(
                    text = if (isPinned) "고정 해제" else "고정",
                    iconRes = if (isPinned) R.drawable.ic_pin_off else R.drawable.ic_pin,
                    onClick = {
                        onPin()
                        onDismiss()
                    }
                )
            }
            if (isOwnPost) {
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
            } else {
                BottomSheetMenuItemGroup(
                    items = listOf(
                        BottomSheetMenuItemData(
                            text = if (isBookmarked) "저장 취소" else "저장",
                            iconRes = if (isBookmarked) R.drawable.ic_bookmark_slash else R.drawable.ic_bookmark,
                            onClick = {
                                onBookmark()
                                onDismiss()
                            }
                        ),
                        BottomSheetMenuItemData(
                            text = "관심없음",
                            iconRes = R.drawable.ic_eye_off,
                            onClick = {
                                onNotInterested()
                                onDismiss()
                            }
                        )
                    )
                )
                // 크리에이터가 커뮤니티에서 다른 사람 글 삭제
                if (canCreatorDelete) {
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
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostOptionBottomSheetOwnPreview() {
    PostOptionBottomSheetPreviewContainer {
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

@HaloLightDarkPreview
@Composable
private fun PostOptionBottomSheetCreatorOwnPreview() {
    PostOptionBottomSheetPreviewContainer {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItem(
                text = "고정",
                iconRes = R.drawable.ic_pin,
                onClick = {}
            )
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

@HaloLightDarkPreview
@Composable
private fun PostOptionBottomSheetCreatorOwnPinnedPreview() {
    PostOptionBottomSheetPreviewContainer {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItem(
                text = "고정 해제",
                iconRes = R.drawable.ic_pin_off,
                onClick = {}
            )
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

@HaloLightDarkPreview
@Composable
private fun PostOptionBottomSheetOtherPreview() {
    PostOptionBottomSheetPreviewContainer {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(
                        text = "저장",
                        iconRes = R.drawable.ic_bookmark,
                        onClick = {}
                    ),
                    BottomSheetMenuItemData(
                        text = "관심없음",
                        iconRes = R.drawable.ic_eye_off,
                        onClick = {}
                    )
                )
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

@Composable
private fun PostOptionBottomSheetPreviewContainer(
    content: @Composable () -> Unit
) {
    HaloPreview {
        content()
    }
}
