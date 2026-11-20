package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick

/** 상품 줄 오른쪽 장바구니 다시 담기 원 버튼 */
@Composable
fun CartCircleButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .bounceClick()
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cart),
            contentDescription = "장바구니 담기",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
