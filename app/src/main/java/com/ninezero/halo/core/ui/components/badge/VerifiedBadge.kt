package com.ninezero.halo.core.ui.components.badge

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R

/** 인증 뱃지 대상 role 여부 */
private fun isVerifiedRole(role: String?): Boolean = role == "CREATOR" || role == "ADMIN"

/** 인증 뱃지 */
@Composable
fun VerifiedBadge(
    role: String?,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    verticalOffset: Dp = 1.dp
) {
    if (isVerifiedRole(role)) {
        Icon(
            painter = painterResource(R.drawable.ic_check_badge_filled),
            contentDescription = "인증된 크리에이터",
            modifier = modifier
                .offset(y = verticalOffset)
                .padding(start = 4.dp)
                .size(size),
            tint = tint
        )
    }
}
