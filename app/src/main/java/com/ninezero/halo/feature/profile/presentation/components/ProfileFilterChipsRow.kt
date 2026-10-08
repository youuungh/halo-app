package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.feature.feed.domain.model.PostContextType

enum class UserPostSortType(val apiValue: String, val displayName: String) {
    LATEST("LATEST", "최신순"),
    OLDEST("OLDEST", "오래된순")
}

@Composable
fun ProfileFilterChipsRow(
    selectedContextType: PostContextType?,
    selectedSortType: UserPostSortType,
    onContextTypeChange: (PostContextType?) -> Unit,
    onSortTypeChange: (UserPostSortType) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterOptions = listOf(
        PostContextType.GENERAL to "포스트",
        PostContextType.COMMUNITY to "커뮤니티"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좌측: 필터 칩
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { (contextType, label) ->
                val isSelected = selectedContextType == contextType
                Surface(
                    onClick = { onContextTypeChange(contextType) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        Color.Transparent
                    },
                    modifier = Modifier.bounceClick()
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }

        // 우측: 정렬 토글 버튼
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    val newSortType = if (selectedSortType == UserPostSortType.LATEST) {
                        UserPostSortType.OLDEST
                    } else {
                        UserPostSortType.LATEST
                    }
                    onSortTypeChange(newSortType)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_up_down),
                contentDescription = "정렬",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectedSortType.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
