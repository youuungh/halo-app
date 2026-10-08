package com.ninezero.halo.core.ui.components.checkbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 체크박스 */
@Composable
fun HaloCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier
                    .size(48.dp)
                    // toggleable, 접근성이 선택 상태까지 읽음
                    .toggleable(
                        value = checked,
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Checkbox,
                        onValueChange = { onClick() }
                    )
            } else {
                Modifier
            }
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .then(
                    if (checked) {
                        Modifier.background(MaterialTheme.colorScheme.onBackground)
                    } else {
                        Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = contentDescription,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun HaloCheckboxPreview() {
    HaloTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Checked", style = MaterialTheme.typography.labelSmall)
            HaloCheckbox(checked = true)

            Text("Unchecked", style = MaterialTheme.typography.labelSmall)
            HaloCheckbox(checked = false)

            Text("Clickable (48dp)", style = MaterialTheme.typography.labelSmall)
            HaloCheckbox(checked = true, onClick = {})
        }
    }
}
