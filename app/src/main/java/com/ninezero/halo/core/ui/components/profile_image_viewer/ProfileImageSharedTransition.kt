package com.ninezero.halo.core.ui.components.profile_image_viewer

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/** 아바타 morph용 SharedTransitionScope */
val LocalProfileImageTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }  // null이면 morph 없음

/** 오버레이 표시 여부 */
val LocalProfileImageOverlayVisible = compositionLocalOf { false }  // 아바타 sharedElement visibility 매칭용

/** 아바타·오버레이 공유 키 */
const val PROFILE_IMAGE_SHARED_KEY = "profile-image-viewer"
