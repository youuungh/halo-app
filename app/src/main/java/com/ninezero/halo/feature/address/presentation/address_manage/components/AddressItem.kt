package com.ninezero.halo.feature.address.presentation.address_manage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetMenuItemData
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetMenuItemGroup
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.theme.CardShape
import com.ninezero.halo.feature.address.domain.model.Address

/** 배송지 카드 */
@Composable
fun AddressItem(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick()
            .cardBorder(CardShape)
            .clip(CardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onEdit
            )
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = address.recipientName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (address.isDefault) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "기본",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenu = true }
                    .bounceClick(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                    contentDescription = "옵션",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column {
            Text(
                text = address.recipientPhone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = listOfNotNull(address.address, address.addressDetail?.takeIf { it.isNotBlank() }, "(${address.zipCode})")
                    .joinToString(" "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!address.memo.isNullOrBlank()) {
                Text(
                    text = address.memo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showMenu) {
        AddressOptionSheet(
            isDefault = address.isDefault,
            onDismiss = { showMenu = false },
            onSetDefault = onSetDefault,
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}

/** 배송지 옵션 시트 */
@Composable
private fun AddressOptionSheet(
    isDefault: Boolean,
    onDismiss: () -> Unit,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val errorColor = MaterialTheme.colorScheme.error
    FloatingBottomSheet(onDismissRequest = onDismiss) {
        Column {
            if (!isDefault) {
                BottomSheetMenuItemGroup(
                    items = listOf(
                        BottomSheetMenuItemData(
                            text = "기본 배송지로 설정",
                            iconRes = R.drawable.ic_location,
                            onClick = {
                                onSetDefault()
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
