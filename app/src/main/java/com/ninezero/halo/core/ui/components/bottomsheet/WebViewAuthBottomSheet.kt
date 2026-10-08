package com.ninezero.halo.core.ui.components.bottomsheet

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/** 토스 결제 인증 WebView 시트 */
@Composable
fun WebViewAuthBottomSheet(  // returnScheme 이동을 가로채 onReturn
    url: String,
    title: String,
    allowedHostSuffixes: List<String>,
    returnScheme: String,
    onReturn: (Uri) -> Unit,
    onDismiss: () -> Unit,
    returnUrlMarker: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hazeState = remember { HazeState() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        contentWindowInsets = { WindowInsets(0) },
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            BottomSheetDragHandle()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    size = 36.dp,
                    iconSize = 20.dp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AuthWebView(
                url = url,
                allowedHostSuffixes = allowedHostSuffixes,
                returnScheme = returnScheme.lowercase(),
                returnUrlMarker = returnUrlMarker,
                onReturn = onReturn,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .hazeSource(hazeState)
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AuthWebView(
    url: String,
    allowedHostSuffixes: List<String>,
    returnScheme: String,
    returnUrlMarker: String?,
    onReturn: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surface

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun handleUri(uri: Uri): Boolean {
        // http 콜백 URL을 마커로 인터셉트
        if (returnUrlMarker != null && uri.toString().contains(returnUrlMarker)) {
            onReturn(uri)
            return true
        }
        return when (uri.scheme?.lowercase()) {
            returnScheme -> {
                onReturn(uri)
                true  // 커스텀 스킴은 로드하지 않고 인터셉트
            }

            "http", "https" -> {
                val host = uri.host?.lowercase() ?: return true
                val allowed = allowedHostSuffixes.any { host == it || host.endsWith(".$it") }
                !allowed  // 허용 호스트가 아니면 차단
            }

            "about", "data" -> false
            else -> true  // intent:// 등 기타 스킴은 차단
        }
    }

    fun loadAuthUrl(webView: WebView) {
        errorMessage = null
        isLoading = true
        webView.loadUrl(url)
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                loadUrl("about:blank")
                destroy()
            }
            webViewRef = null
        }
    }

    Box(modifier = modifier.background(backgroundColor)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    // 시트 내 overscroll 제거
                    overScrollMode = View.OVER_SCROLL_NEVER
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    setBackgroundColor(backgroundColor.toArgb())

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val uri = request?.url ?: return false
                            return handleUri(uri)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            val uri = url?.let(Uri::parse) ?: return false
                            return handleUri(uri)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                errorMessage = error?.description?.toString()
                                    ?: "페이지를 불러오지 못했습니다"
                                isLoading = false
                            }
                        }
                    }

                    webViewRef = this
                    loadAuthUrl(this)
                }
            }
        )

        if (isLoading && errorMessage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(size = LoadingIndicatorSize.Large)
            }
        }

        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "페이지를 불러오지 못했습니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { webViewRef?.let(::loadAuthUrl) }) {
                    Text("다시 시도")
                }
            }
        }
    }
}
