package com.ninezero.halo.feature.coupon.presentation.my_coupons.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.chip.HaloFilterChip
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import dev.chrisbanes.haze.HazeState

@Composable
internal fun CouponFilterChips(
    selectedStatus: UserCouponStatus?,
    onFilterByStatus: (UserCouponStatus?) -> Unit,
    hazeState: HazeState? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HaloFilterChip(
            selected = selectedStatus == null,
            label = "전체",
            onClick = { onFilterByStatus(null) },
            hazeState = hazeState
        )
        HaloFilterChip(
            selected = selectedStatus == UserCouponStatus.AVAILABLE,
            label = "사용 가능",
            onClick = { onFilterByStatus(UserCouponStatus.AVAILABLE) },
            hazeState = hazeState
        )
        HaloFilterChip(
            selected = selectedStatus == UserCouponStatus.USED,
            label = "사용완료",
            onClick = { onFilterByStatus(UserCouponStatus.USED) },
            hazeState = hazeState
        )
        HaloFilterChip(
            selected = selectedStatus == UserCouponStatus.EXPIRED,
            label = "만료",
            onClick = { onFilterByStatus(UserCouponStatus.EXPIRED) },
            hazeState = hazeState
        )
    }
}
