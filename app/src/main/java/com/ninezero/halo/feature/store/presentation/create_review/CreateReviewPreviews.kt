package com.ninezero.halo.feature.store.presentation.create_review

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun CreateReviewScreenPreview() {
    HaloScreenPreview {
        CreateReviewScreenContent(
            state = CreateReviewState(
                productName = "프리미엄 블루투스 이어폰 Pro Max 프리미엄 블루투스 이어폰 Pro Max 프리미엄 블루투스 이어폰 Pro Max",
                productImageUrl = null,
                myDisplayName = "사용자"
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreateReviewScreenWithContentPreview() {
    HaloScreenPreview {
        CreateReviewScreenContent(
            state = CreateReviewState(
                productName = "프리미엄 블루투스 이어폰 Pro Max ",
                productImageUrl = null,
                rating = 4,
                content = "음질이 정말 뛰어납니다! 노이즈 캔슬링도 완벽하고 배터리도 오래가요.",
                selectedMediaUris = listOf(
                    "https://picsum.photos/200/200".toUri(),
                    "https://picsum.photos/201/200".toUri()
                ),
                myDisplayName = "사용자"
            )
        )
    }
}
