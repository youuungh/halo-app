package com.ninezero.halo.core.ui.components.switch

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}

/** 스위치 */
@Composable
fun HaloSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        thumbContent = {
            Crossfade(targetState = checked, label = "switch_icon") { isChecked ->
                Icon(
                    painter = painterResource(
                        if (isChecked) R.drawable.ic_check else R.drawable.ic_close
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        },
        interactionSource = remember { NoRippleInteractionSource() },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = Color.Transparent,
            checkedIconColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
            uncheckedBorderColor = Color.Transparent,
            uncheckedIconColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun HaloSwitchPreview() {
    HaloTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ON", style = MaterialTheme.typography.labelSmall)
            HaloSwitch(checked = true, onCheckedChange = {})

            Text("OFF", style = MaterialTheme.typography.labelSmall)
            HaloSwitch(checked = false, onCheckedChange = {})

            Text("Disabled ON", style = MaterialTheme.typography.labelSmall)
            HaloSwitch(checked = true, onCheckedChange = {}, enabled = false)

            Text("Disabled OFF", style = MaterialTheme.typography.labelSmall)
            HaloSwitch(checked = false, onCheckedChange = {}, enabled = false)
        }
    }
}
