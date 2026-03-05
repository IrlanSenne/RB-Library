package com.pop.round_border_image

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@androidx.compose.runtime.Composable
fun RoundBorderImage() {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadUrl("https://popapp.pt")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}