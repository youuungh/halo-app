package com.ninezero.halo.core.ui.components.toggle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 결제수단·쿠폰 단일 선택 표시 */
@Composable
fun SelectionRadio(
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(20.dp)
            .then(
                when {
                    !enabled -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    selected -> Modifier.border(2.dp, primary, CircleShape)
                    else -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected && enabled) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(primary, CircleShape)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SelectionRadioPreview() {
    HaloSurfacePreview {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectionRadio(selected = true)
            SelectionRadio(selected = false)
            SelectionRadio(selected = true, enabled = false)
        }
    }
}
