package com.ninezero.halo.feature.store.presentation.sales_manage.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer

fun LazyListScope.salesManageSkeleton() {
    items(5) {
        SalesOrderCardSkeleton()
    }
}

@Composable
private fun SalesOrderCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(20.dp))
            .shimmer()
            .height(150.dp)
    )
}
