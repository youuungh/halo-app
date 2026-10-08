package com.ninezero.halo.feature.point.presentation

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.util.toEpochMillis
import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.point.domain.model.PointHistory
import com.ninezero.halo.feature.point.domain.model.PointType
import java.time.LocalDateTime
import java.time.YearMonth

internal fun previewPointState(): PointState {
    return PointState(
        isLoading = false,
        balance = previewPointBalance(),
        histories = previewPointHistories,
        selectedMonth = YearMonth.now(),
        registrationMonth = YearMonth.now().minusMonths(6)
    )
}

internal fun previewPointLoadingMoreState(): PointState {
    return PointState(
        isLoading = true,
        balance = previewPointBalance(),
        histories = previewPointHistories,
        hasMore = true,
        selectedMonth = YearMonth.now(),
        registrationMonth = YearMonth.now().minusMonths(6)
    )
}

internal fun previewPointEmptyState(): PointState {
    return PointState(
        isLoading = false,
        balance = PointBalance(totalPoints = 0),
        histories = emptyList()
    )
}

internal fun previewPointLoadingState(): PointState {
    return PointState(isLoading = true)
}

private val previewPointHistories = listOf(
    previewPointHistory(1, PointType.EARN, 500, "구매 적립 (주문 #12345)", 12345, hoursAgo = 2),
    previewPointHistory(2, PointType.USE, 1000, "포인트 사용", 12346, daysAgo = 1),
    previewPointHistory(3, PointType.EARN, 200, "리뷰 작성 보상", daysAgo = 3),
    previewPointHistory(4, PointType.EXPIRE, 100, "포인트 만료", daysAgo = 5),
    previewPointHistory(5, PointType.EARN, 1000, "이벤트 당첨", daysAgo = 7),
    previewPointHistory(6, PointType.EARN, 350, "구매 적립 (주문 #12347)", 12347, daysAgo = 10),
    previewPointHistory(7, PointType.REFUND, 800, "주문 취소 환불", 12340, daysAgo = 12),
    previewPointHistory(8, PointType.USE, 2000, "포인트 사용", 12348, daysAgo = 14),
    previewPointHistory(9, PointType.EARN, 150, "출석 체크 보상", daysAgo = 15),
    previewPointHistory(10, PointType.EARN, 5000, "신규 가입 축하 포인트", daysAgo = 20),
    previewPointHistory(11, PointType.EARN, 720, "구매 적립 (주문 #12349)", 12349, daysAgo = 22),
    previewPointHistory(12, PointType.USE, 3000, "포인트 사용", 12350, daysAgo = 25),
    previewPointHistory(13, PointType.EARN, 100, "리뷰 작성 보상", daysAgo = 27),
    previewPointHistory(14, PointType.EXPIRE, 250, "포인트 만료", daysAgo = 30),
    previewPointHistory(15, PointType.REFUND, 1500, "주문 취소 환불", 12335, daysAgo = 33)
)

private fun previewPointBalance(): PointBalance {
    return PointBalance(
        totalPoints = 12500,
        expiringPoints = 500,
        expiresAt = LocalDateTime.of(2025, 1, 23, 0, 0).toEpochMillis()
    )
}

private fun previewPointHistory(
    id: Int,
    type: PointType,
    amount: Int,
    description: String,
    orderId: Int? = null,
    hoursAgo: Long = 0,
    daysAgo: Long = 0,
    now: Long = System.currentTimeMillis()
): PointHistory {
    return PointHistory(
        id = id,
        type = type,
        amount = amount,
        description = description,
        createdAt = now - hoursAgo * 3_600_000L - daysAgo * 86_400_000L
    )
}

@HaloLightDarkPreview
@Composable
private fun PointScreenPreview() {
    HaloScreenPreview {
        PointScreenContent(
            state = previewPointState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PointScreenLoadingMorePreview() {
    HaloScreenPreview {
        PointScreenContent(
            state = previewPointLoadingMoreState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PointScreenLoadingPreview() {
    HaloScreenPreview {
        PointScreenContent(
            state = previewPointLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PointScreenEmptyPreview() {
    HaloScreenPreview {
        PointScreenContent(
            state = previewPointEmptyState(),
            onIntent = {}
        )
    }
}
