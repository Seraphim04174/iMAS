package com.imas.imas.android

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var canGoBack by remember { mutableStateOf(false) }
                    // Компонент WebView
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                webView = this

                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.setSupportZoom(true)
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                settings.userAgentString = WebSettings.getDefaultUserAgent(context)

                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        canGoBack = view?.canGoBack() ?: false
                                    }
                                }

                                webChromeClient = WebChromeClient()

                                setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                                        setMimeType(mimeType)
                                        addRequestHeader("User-Agent", WebSettings.getDefaultUserAgent(context))
                                        setDescription("Загрузка файла...")
                                        setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                                        allowScanningByMediaScanner()
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        setDestinationInExternalPublicDir(
                                            Environment.DIRECTORY_DOWNLOADS,
                                            URLUtil.guessFileName(url, contentDisposition, mimeType)
                                        )
                                    }

                                    val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                    dm.enqueue(request)
                                    Toast.makeText(context, "Загрузка началась...", Toast.LENGTH_SHORT).show()
                                }

                                loadUrl("https://mobile.imas.kz/ru/tape?id=17417&p=1")
                            }
                        },
                        update = { view ->
                            canGoBack = view.canGoBack()
                        }
                    )

                    // Обработка кнопки "Назад"
                    BackHandler(enabled = canGoBack) {
                        if (webView?.canGoBack() == true) {
                            webView?.goBack()
                        }
                    }
                }
            }
        }
    }
}
