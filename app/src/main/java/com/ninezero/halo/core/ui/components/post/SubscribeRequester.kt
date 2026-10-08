package com.ninezero.halo.core.ui.components.post

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSubscribeRequester =
    staticCompositionLocalOf<(creatorId: Int, creatorName: String) -> Unit> {
        { _, _ -> }
    }
