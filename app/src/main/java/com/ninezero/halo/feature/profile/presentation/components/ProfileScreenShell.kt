package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.profile_image_viewer.LocalProfileImageOverlayVisible
import com.ninezero.halo.core.ui.components.profile_image_viewer.LocalProfileImageTransitionScope
import com.ninezero.halo.core.ui.components.profile_image_viewer.ProfileImageViewerOverlay
import com.ninezero.halo.core.ui.components.profile_image_viewer.ProfileImageViewerState
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

class ProfileShellCallbacks(
    val onShowMediaViewer: (items: List<ViewerMediaItem>, initialIndex: Int, ownerKey: String?) -> Unit,
    val onShowProfileImageViewer: (imageUrl: String?) -> Unit,
)

@Composable
fun ProfileScreenShell(
    onResume: () -> Unit,
    externalShowProfileImageViewer: ((String?) -> Unit)? = null,  // null이면 셸이 자체 오버레이 표시
    sharedTransitionScope: SharedTransitionScope? = null,
    content: @Composable (ProfileShellCallbacks) -> Unit
) {
    // 복귀 가드는 saveable이어야 유지
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }
    val parentViewerHost = LocalViewerOverlayHostState.current
    val localViewerHost = rememberViewerOverlayHostState()
    val viewerHostState = parentViewerHost ?: localViewerHost
    var profileImageViewerState by remember { mutableStateOf(ProfileImageViewerState()) }
    val profileViewerHazeState = remember { HazeState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            onResume()
        } else {
            hasInitialResumeHandled = true
        }
    }

    val callbacks = ProfileShellCallbacks(
        onShowMediaViewer = { items, index, ownerKey ->
            viewerHostState.showMediaViewer(items, index, ownerKey)
        },
        onShowProfileImageViewer = { imageUrl ->
            if (externalShowProfileImageViewer != null) {
                externalShowProfileImageViewer(imageUrl)
            } else {
                profileImageViewerState = ProfileImageViewerState(
                    isVisible = true,
                    imageUrl = imageUrl
                )
            }
        }
    )

    // 하위 공유 전환용 overlayVisible
    val provideShared = externalShowProfileImageViewer == null && sharedTransitionScope != null
    val content2: @Composable () -> Unit = {
        CompositionLocalProvider(LocalViewerOverlayHostState provides viewerHostState) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (externalShowProfileImageViewer == null) {
                                Modifier.hazeSource(profileViewerHazeState)
                            } else Modifier
                        )
                ) {
                    content(callbacks)
                }

                if (externalShowProfileImageViewer == null) {
                    ProfileImageViewerOverlay(
                        state = profileImageViewerState,
                        onDismiss = {
                            profileImageViewerState =
                                profileImageViewerState.copy(isVisible = false)
                        },
                        hazeState = profileViewerHazeState,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }

                if (parentViewerHost == null) {
                    ViewerOverlayHost(
                        state = viewerHostState,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }
            }
        }
    }

    if (provideShared) {
        CompositionLocalProvider(
            LocalProfileImageTransitionScope provides sharedTransitionScope,
            LocalProfileImageOverlayVisible provides profileImageViewerState.isVisible,
            LocalMediaSharedTransitionScope provides sharedTransitionScope,
            LocalActiveViewerMediaKey provides viewerHostState.activeMediaKey
        ) {
            content2()
        }
    } else {
        content2()
    }
}
