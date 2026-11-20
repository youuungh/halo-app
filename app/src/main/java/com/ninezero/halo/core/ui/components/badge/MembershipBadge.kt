package com.ninezero.halo.core.ui.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.MembershipColors

private val MembershipBadgeShape = RoundedCornerShape(5.dp)

/** 멤버십 표시 배지 */
@Composable
fun MembershipBadge(
    modifier: Modifier = Modifier,
    text: String = "멤버십",
    onPhoto: Boolean = false,
    large: Boolean = false
) {
    Box(
        modifier = modifier
            .then(if (onPhoto) Modifier.background(MaterialTheme.colorScheme.surface, MembershipBadgeShape) else Modifier)
            .background(MembershipColors.badgeBrush, MembershipBadgeShape)
            .padding(horizontal = if (large) 7.dp else 6.dp, vertical = if (large) 3.dp else 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (large) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MembershipColors.label,
            maxLines = 1
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipBadgePreview() {
    HaloTheme {
        Surface {
            Column(modifier = Modifier.padding(12.dp)) {
                MembershipBadge()
                MembershipBadge(text = "멤버십 전용", modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
