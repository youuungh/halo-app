package com.ninezero.halo.feature.feed.presentation.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.snackbar.LocalSnackbarRelay
import com.ninezero.halo.core.util.showToast
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ReportHost(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val viewModel: ReportViewModel = hiltViewModel()
    val snackbarRelay = LocalSnackbarRelay.current
    val scope = rememberCoroutineScope()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ReportSideEffect.ShowMessage -> {
                val hostState = snackbarRelay.current
                if (hostState != null) {
                    scope.launch { hostState.showSnackbar(effect.message) }
                } else {
                    context.showToast(effect.message)
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalReportRequester provides { targetType, targetId ->
            viewModel.onIntent(ReportIntent.Submit(targetType, targetId))
        }
    ) {
        content()
    }
}
