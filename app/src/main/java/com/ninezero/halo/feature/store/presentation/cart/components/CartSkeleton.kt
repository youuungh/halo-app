package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer

@Composable
fun CartSkeleton(topPadding: Dp) {
    LazyColumn(
        userScrollEnabled = false,
        contentPadding = PaddingValues(top = topPadding)
    ) {
        items(listOf(0, 1, 2, 3)) { groupIndex ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = if (groupIndex == 0) 0.dp else 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shimmer()
                    .height(150.dp)
            )
        }
    }
}
