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

/** 상품 옵션 시트 */
@Composable
fun ProductOptionBottomSheet(
    isDiscontinued: Boolean,
    isSoldOut: Boolean,
    hasDeal: Boolean,
    onDismiss: () -> Unit,
    onSetDeal: () -> Unit,
    onClearDeal: () -> Unit,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
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
            // 타임딜 + 판매 상태
            val operationalItems = buildList {
                // 예약 딜도 포함, 취소 가능
                add(
                    BottomSheetMenuItemData(
                        text = if (hasDeal) "타임딜 해제" else "타임딜 설정",
                        iconRes = R.drawable.ic_timer,
                        onClick = {
                            if (hasDeal) onClearDeal() else onSetDeal()
                            onDismiss()
                        }
                    )
                )
                // 품절은 재고로만 관리되므로 판매 상태 토글 불가 → 행 숨김
                if (!isSoldOut) {
                    add(
                        BottomSheetMenuItemData(
                            text = if (isDiscontinued) "판매 재개" else "판매 종료",
                            iconRes = if (isDiscontinued) R.drawable.ic_play else R.drawable.ic_pause,
                            onClick = {
                                onToggleStatus()
                                onDismiss()
                            }
                        )
                    )
                }
            }
            BottomSheetMenuItemGroup(items = operationalItems)

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
private fun ProductOptionBottomSheetPreview() {
    HaloTheme {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(text = "타임딜 설정", iconRes = R.drawable.ic_timer, onClick = {}),
                    BottomSheetMenuItemData(text = "판매 종료", iconRes = R.drawable.ic_pause, onClick = {})
                )
            )
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(text = "수정", iconRes = R.drawable.ic_edit, onClick = {}),
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
private fun ProductOptionBottomSheetDealActivePreview() {
    HaloTheme {
        val errorColor = MaterialTheme.colorScheme.error
        Column {
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(text = "타임딜 해제", iconRes = R.drawable.ic_timer, onClick = {}),
                    BottomSheetMenuItemData(text = "판매 재개", iconRes = R.drawable.ic_play, onClick = {})
                )
            )
            BottomSheetMenuItemGroup(
                items = listOf(
                    BottomSheetMenuItemData(text = "수정", iconRes = R.drawable.ic_edit, onClick = {}),
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
