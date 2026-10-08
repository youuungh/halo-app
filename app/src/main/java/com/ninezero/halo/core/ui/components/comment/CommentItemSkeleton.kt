package com.ninezero.halo.core.ui.components.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 댓글 스켈레톤 */
@Composable
fun CommentItemSkeleton(
    modifier: Modifier = Modifier,
    showLine: Boolean = false,
    horizontalPadding: Dp = 8.dp,
    avatarSize: Dp = 32.dp,
    avatarSpacing: Dp = 12.dp,
    lineWidth: Dp = 1.5.dp,
    lineColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .height(IntrinsicSize.Min)
    ) {
        // 왼쪽: 프로필 + 연결선
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(avatarSize)
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .shimmer()
            )

            if (showLine) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(lineWidth)
                        .weight(1f)
                        .background(lineColor)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.width(avatarSpacing))

        // 오른쪽: 컨텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (showLine) 12.dp else 8.dp)
        ) {
            // 이름 + 시간
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(35.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 본문
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 액션 버튼
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentItemSkeletonPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentItemSkeleton(
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentItemSkeletonWithLinePreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                CommentItemSkeleton(showLine = true)
                CommentItemSkeleton(showLine = true)
                CommentItemSkeleton(showLine = false)
            }
        }
    }
}
