package com.ninezero.halo.feature.address.presentation.components

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun DaumPostcodeBottomSheet(
    onDismiss: () -> Unit,
    onAddressSelected: (zipCode: String, address: String) -> Unit
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
                    text = "주소 검색",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DaumPostcodeWebView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .hazeSource(hazeState),
                onAddressSelected = onAddressSelected
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun DaumPostcodeWebView(
    modifier: Modifier = Modifier,
    onAddressSelected: (zipCode: String, address: String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val backgroundColor = scheme.surface

    val html = remember(
        scheme.surface, scheme.background, scheme.onSurface, scheme.primary, scheme.outline
    ) {
        buildPostcodeHtml(
            bgColor = scheme.surface.toHex(),
            searchBgColor = scheme.surface.toHex(),
            contentBgColor = scheme.surface.toHex(),
            pageBgColor = scheme.surface.toHex(),
            textColor = scheme.onSurface.toHex(),
            emphTextColor = scheme.primary.toHex(),
            outlineColor = scheme.outline.toHex()
        )
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadPostcodeHtml(webView: WebView) {
        errorMessage = null
        isLoading = true
        webView.loadDataWithBaseURL(
            "https://t1.kakaocdn.net",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                removeJavascriptInterface(JS_BRIDGE_NAME)
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
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    // 시트 내 overscroll 제거
                    overScrollMode = View.OVER_SCROLL_NEVER

                    setBackgroundColor(backgroundColor.toArgb())

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    addJavascriptInterface(
                        PostcodeJSInterface(
                            onResult = { zipCode, address ->
                                onAddressSelected(zipCode, address)
                            },
                            onReady = { post { isLoading = false } }
                        ),
                        JS_BRIDGE_NAME
                    )

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val uri = request?.url ?: return false
                            return !isAllowedUri(uri)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            val uri = url?.let(Uri::parse) ?: return false
                            return !isAllowedUri(uri)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                errorMessage = error?.description?.toString()
                                    ?: "주소 검색 페이지를 불러오지 못했습니다"
                                isLoading = false
                            }
                        }
                    }

                    webViewRef = this
                    loadPostcodeHtml(this)
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
                    text = "주소 검색을 불러오지 못했습니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { webViewRef?.let(::loadPostcodeHtml) }) {
                    Text("다시 시도")
                }
            }
        }
    }
}

private class PostcodeJSInterface(
    private val onResult: (String, String) -> Unit,
    private val onReady: () -> Unit
) {
    @JavascriptInterface
    fun processAddress(zonecode: String, address: String) {
        onResult(zonecode, address)
    }

    @JavascriptInterface
    fun onPostcodeReady() {
        onReady()
    }
}

private const val JS_BRIDGE_NAME = "Android"

private val ALLOWED_POSTCODE_HOST_SUFFIXES = listOf(
    "kakao.com",
    "kakaocdn.net",
    "daum.net"
)

private fun isAllowedUri(uri: Uri): Boolean {
    return when (uri.scheme?.lowercase()) {
        "about", "data" -> true
        "http", "https" -> {
            val host = uri.host?.lowercase() ?: return false
            ALLOWED_POSTCODE_HOST_SUFFIXES.any { suffix ->
                host == suffix || host.endsWith(".$suffix")
            }
        }
        else -> false
    }
}

private fun Color.toHex(): String = String.format("#%06X", 0xFFFFFF and toArgb())

private fun buildPostcodeHtml(
    bgColor: String,
    searchBgColor: String,
    contentBgColor: String,
    pageBgColor: String,
    textColor: String,
    emphTextColor: String,
    outlineColor: String
): String {
    val themeJs = """
        theme: {
            bgColor: '$bgColor',
            searchBgColor: '$searchBgColor',
            contentBgColor: '$contentBgColor',
            pageBgColor: '$pageBgColor',
            textColor: '$textColor',
            queryTextColor: '$textColor',
            postcodeTextColor: '$emphTextColor',
            emphTextColor: '$emphTextColor',
            outlineColor: '$outlineColor'
        },
    """.trimIndent()

    return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        html, body { margin: 0; padding: 0; background: $bgColor; }
        body { position: fixed; top: 0; left: 0; right: 0; bottom: 0; }
        #postcode-container { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: $bgColor; }
    </style>
</head>
<body>
    <div id="postcode-container"></div>
    <script>
        var notifiedReady = false;
        function notifyReady() {
            if (notifiedReady) return;
            notifiedReady = true;
            Android.onPostcodeReady();
        }
        function execDaumPostcode() {
            new kakao.Postcode({
                $themeJs
                hideEngBtn: true,
                pleaseReadGuide: 0,
                oncomplete: function(data) {
                    var address = (data.userSelectedType === 'R') ? data.roadAddress : data.jibunAddress;
                    var extraAddress = '';

                    if (data.userSelectedType === 'R') {
                        if (data.bname !== '' && /[동|로|가]${'$'}/g.test(data.bname)) {
                            extraAddress += data.bname;
                        }

                        if (data.buildingName !== '' && data.apartment === 'Y') {
                            extraAddress += (extraAddress !== '' ? ', ' + data.buildingName : data.buildingName);
                        }

                        if (extraAddress !== '') {
                            address += ' (' + extraAddress + ')';
                        }
                    }

                    Android.processAddress(data.zonecode, address);
                },
                onresize: function() { notifyReady(); },
                width: '100%',
                height: '100%'
            }).embed(document.getElementById('postcode-container'));
            setTimeout(notifyReady, 2500);
        }
    </script>
    <script src="https://t1.kakaocdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
            onload="execDaumPostcode()"></script>
</body>
</html>
    """.trimIndent()
}
