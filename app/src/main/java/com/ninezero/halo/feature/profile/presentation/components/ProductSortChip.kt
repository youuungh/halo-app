package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

enum class ProductSortType(val apiValue: String, val displayName: String) {
    LATEST("LATEST", "최신순"),
    PRICE_LOW("PRICE_LOW", "낮은 가격순"),
    PRICE_HIGH("PRICE_HIGH", "높은 가격순")
}

@Composable
fun ProductSortChip(
    currentSort: ProductSortType,
    onSortChange: (ProductSortType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Surface(
        onClick = { showBottomSheet = true },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_up_down),
                contentDescription = "정렬",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currentSort.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showBottomSheet) {
        SortBottomSheet(
            currentSort = currentSort,
            onSortChange = onSortChange,
            onDismiss = { showBottomSheet = false }
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductSortChipLatestPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductSortChip(
                currentSort = ProductSortType.LATEST,
                onSortChange = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductSortChipPriceLowPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductSortChip(
                currentSort = ProductSortType.PRICE_LOW,
                onSortChange = {}
            )
        }
    }
}
