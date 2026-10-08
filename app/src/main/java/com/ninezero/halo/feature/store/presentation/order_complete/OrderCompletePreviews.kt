package com.ninezero.halo.feature.store.presentation.order_complete

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun OrderCompleteScreenPreview() {
    HaloScreenPreview {
        OrderCompleteScreenContent(orderNumber = "2026071112345678")
    }
}
