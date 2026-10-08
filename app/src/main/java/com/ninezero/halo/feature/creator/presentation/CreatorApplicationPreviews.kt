package com.ninezero.halo.feature.creator.presentation

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.creator.domain.model.CreatorApplication
import com.ninezero.halo.feature.creator.domain.model.CreatorApplicationStatus

internal fun previewCreatorApplicationFormState(): CreatorApplicationState {
    return CreatorApplicationState(
        isLoading = false,
        application = null,
        reason = "디자인 관련 콘텐츠를 공유하고 싶습니다"
    )
}

internal fun previewCreatorApplicationPendingState(): CreatorApplicationState {
    return CreatorApplicationState(
        isLoading = false,
        application = previewCreatorApplication(CreatorApplicationStatus.PENDING)
    )
}

internal fun previewCreatorApplicationApprovedState(): CreatorApplicationState {
    return CreatorApplicationState(
        isLoading = false,
        application = previewCreatorApplication(
            status = CreatorApplicationStatus.APPROVED,
            createdAt = "2026-03-10T10:00:00".parseTimestamp(),
            reviewedAt = "2026-03-15T14:00:00".parseTimestamp()
        )
    )
}

internal fun previewCreatorApplicationRejectedState(): CreatorApplicationState {
    return CreatorApplicationState(
        isLoading = false,
        application = previewCreatorApplication(
            status = CreatorApplicationStatus.REJECTED,
            reason = "그냥 하고 싶어서요",
            portfolioUrl = null,
            rejectionReason = "신청 사유가 구체적이지 않습니다. 크리에이터로서 어떤 콘텐츠나 상품을 제공할 계획인지 구체적으로 작성해주세요.",
            createdAt = "2026-03-10T10:00:00".parseTimestamp(),
            reviewedAt = "2026-03-15T14:00:00".parseTimestamp()
        )
    )
}

internal fun previewCreatorApplicationRevokedState(): CreatorApplicationState {
    return CreatorApplicationState(
        isLoading = false,
        application = previewCreatorApplication(
            status = CreatorApplicationStatus.REVOKED,
            rejectionReason = "커뮤니티 가이드라인을 반복적으로 위반하여 크리에이터 자격이 해제되었습니다.",
            createdAt = "2026-03-01T10:00:00".parseTimestamp(),
            reviewedAt = "2026-03-18T14:00:00".parseTimestamp()
        )
    )
}

internal fun previewCreatorApplicationLoadingState(): CreatorApplicationState {
    return CreatorApplicationState(isLoading = true)
}

internal fun previewCreatorApplicationErrorState(): CreatorApplicationState {
    return CreatorApplicationState(
        isLoading = false,
        error = "신청 정보를 불러올 수 없습니다"
    )
}

private fun previewCreatorApplication(
    status: CreatorApplicationStatus,
    reason: String = "디자인 관련 콘텐츠를 공유하고 싶습니다",
    portfolioUrl: String? = "https://portfolio.example.com",
    rejectionReason: String? = null,
    createdAt: Long = "2026-03-17T10:00:00".parseTimestamp(),
    reviewedAt: Long? = null
): CreatorApplication {
    return CreatorApplication(
        reason = reason,
        portfolioUrl = portfolioUrl,
        status = status,
        rejectionReason = rejectionReason,
        createdAt = createdAt,
        reviewedAt = reviewedAt
    )
}

@HaloLightDarkPreview
@Composable
private fun CreatorApplicationScreenFormPreview() {
    HaloScreenPreview {
        CreatorApplicationScreenContent(
            state = previewCreatorApplicationFormState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorApplicationScreenPendingPreview() {
    HaloScreenPreview {
        CreatorApplicationScreenContent(
            state = previewCreatorApplicationPendingState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorApplicationScreenApprovedPreview() {
    HaloScreenPreview {
        CreatorApplicationScreenContent(
            state = previewCreatorApplicationApprovedState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorApplicationScreenRejectedPreview() {
    HaloScreenPreview {
        CreatorApplicationScreenContent(
            state = previewCreatorApplicationRejectedState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorApplicationScreenRevokedPreview() {
    HaloScreenPreview {
        CreatorApplicationScreenContent(
            state = previewCreatorApplicationRevokedState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorApplicationScreenLoadingPreview() {
    HaloScreenPreview {
        CreatorApplicationScreenContent(
            state = previewCreatorApplicationLoadingState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorApplicationScreenErrorPreview() {
    HaloScreenPreview {
        CreatorApplicationScreenContent(
            state = previewCreatorApplicationErrorState()
        )
    }
}
