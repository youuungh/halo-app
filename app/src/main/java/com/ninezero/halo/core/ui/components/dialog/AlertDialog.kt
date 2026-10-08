package com.ninezero.halo.core.ui.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloPreview
import com.ninezero.halo.ui.theme.Pretendard

/** 알림 다이얼로그 */
@Composable
fun AlertDialog(
    title: String? = null,
    message: String? = null,
    buttonText: String = "확인",
    buttonColor: Color = MaterialTheme.colorScheme.onBackground,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
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

                        if (title != null && message != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (message != null) {
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

                // 버튼
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
                        text = buttonText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        color = buttonColor
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun AlertDialogTitleOnlyPreview() {
    AlertDialogPreviewContainer {
        AlertDialog(
            title = "알림",
            onDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun AlertDialogMessageOnlyPreview() {
    AlertDialogPreviewContainer {
        AlertDialog(
            message = "저장되었습니다.",
            onDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun AlertDialogBothPreview() {
    AlertDialogPreviewContainer {
        AlertDialog(
            title = "알림",
            message = "변경사항이 저장되었습니다.",
            onDismiss = {}
        )
    }
}

@Composable
private fun AlertDialogPreviewContainer(
    content: @Composable () -> Unit
) {
    HaloPreview {
        content()
    }
}
