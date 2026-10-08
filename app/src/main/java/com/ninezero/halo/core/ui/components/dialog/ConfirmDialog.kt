package com.ninezero.halo.core.ui.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloPreview
import com.ninezero.halo.ui.theme.Pretendard

/** 확인 다이얼로그 */
@Composable
fun ConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    profileImageUrl: String? = null,
    title: String? = null,
    message: String? = null,
    annotatedMessage: AnnotatedString? = null,
    confirmText: String = "확인",
    dismissText: String = "취소",
    neutralText: String? = null,
    confirmColor: Color = MaterialTheme.colorScheme.onBackground,
    dismissColor: Color = MaterialTheme.colorScheme.onBackground,
    onNeutral: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onNeutral ?: onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 80.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                // 내용 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (profileImageUrl != null) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "프로필 이미지",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                                error = painterResource(R.drawable.ic_profile_placeholder),
                                fallback = painterResource(R.drawable.ic_profile_placeholder)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (title != null) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (title != null && (message != null || annotatedMessage != null)) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (annotatedMessage != null) {
                            Text(
                                text = annotatedMessage,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = Pretendard,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        } else if (message != null) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = Pretendard,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // 확인 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                bounded = true,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        ) { onConfirm() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confirmText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        color = confirmColor
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // 취소 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                bounded = true,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        ) { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dismissText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Pretendard,
                        color = dismissColor
                    )
                }

                // 3번째 버튼
                if (neutralText != null && onNeutral != null) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = true,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            ) { onNeutral() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = neutralText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = Pretendard,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ConfirmDialogTitleOnlyPreview() {
    ConfirmDialogPreviewContainer {
        ConfirmDialog(
            title = "타이틀타이틀타이틀타이틀타이틀",
            confirmText = "확인",
            confirmColor = MaterialTheme.colorScheme.onBackground,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ConfirmDialogMessageOnlyPreview() {
    ConfirmDialogPreviewContainer {
        ConfirmDialog(
            message = "내용내용내용내용내용내용내용내용내용",
            confirmText = "확인",
            confirmColor = MaterialTheme.colorScheme.onBackground,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ConfirmDialogBothPreview() {
    ConfirmDialogPreviewContainer {
        ConfirmDialog(
            title = "타이틀타이틀타이틀타이틀타이틀",
            message = "내용내용내용내용내용내용내용내용내용",
            confirmText = "확인",
            confirmColor = MaterialTheme.colorScheme.onBackground,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ConfirmDialogThreeButtonsPreview() {
    ConfirmDialogPreviewContainer {
        ConfirmDialog(
            title = "임시 저장할까요?",
            confirmText = "저장",
            dismissText = "저장 안 함",
            neutralText = "계속 수정",
            dismissColor = MaterialTheme.colorScheme.error,
            onConfirm = {},
            onDismiss = {},
            onNeutral = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ConfirmDialogWithProfileImagePreview() {
    ConfirmDialogPreviewContainer {
        ConfirmDialog(
            profileImageUrl = "https://picsum.photos/100",
            title = "사용자님을 차단하시겠어요?",
            confirmText = "차단",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Composable
private fun ConfirmDialogPreviewContainer(
    content: @Composable () -> Unit
) {
    HaloPreview {
        content()
    }
}
