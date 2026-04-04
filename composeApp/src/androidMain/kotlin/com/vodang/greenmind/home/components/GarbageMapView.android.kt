package com.vodang.greenmind.home.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.vodang.greenmind.location.Location

private const val TAG = "GM_MAP"
private const val ROUTE_TAG = "GM_ROUTE"

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun GarbageHeatMapView(
    points: List<GarbageMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(webViewRef.value, center, zoomLevel) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        val lat = center?.latitude ?: 16.0544
        val lng = center?.longitude ?: 108.2022
        Log.d(TAG, "UPDATE_VIEW lat=$lat lng=$lng zoom=$zoomLevel")
        wv.evaluateJavascript("updateView($lat,$lng,$zoomLevel);", null)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val initLat = center?.latitude ?: 16.0544
            val initLng = center?.longitude ?: 108.2022
            Log.d(TAG, "WEBVIEW_FACTORY lat=$initLat lng=$initLng zoom=$zoomLevel")
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                // Prevent the parent scroll container from stealing touch events so the
                // WebView can handle map pan/zoom gestures itself.
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        Log.d(TAG, "JS[${msg.messageLevel()}] ${msg.message()} @ ${msg.sourceId()}:${msg.lineNumber()}")
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d(TAG, "PAGE_LOADED url=$url")
                        webViewRef.value = view
                    }
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        Log.e(TAG, "RESOURCE_ERROR url=${request?.url} code=${error?.errorCode} desc=${error?.description}")
                    }
                }
                // Base URL points to assets so relative paths (leaflet.js etc.) resolve correctly.
                loadDataWithBaseURL(
                    "file:///android_asset/",
                    buildLeafletHtml(points, initLat, initLng, zoomLevel.toDouble(), localScripts = true),
                    "text/html", "UTF-8", null
                )
            }
        },
        onRelease = { webView ->
            Log.d(TAG, "WEBVIEW_RELEASE")
            webViewRef.value = null
            webView.stopLoading()
            webView.destroy()
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun RouteMapView(
    points: List<RouteMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(webViewRef.value, center, zoomLevel) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        val lat = center?.latitude ?: 16.0544
        val lng = center?.longitude ?: 108.2022
        Log.d(ROUTE_TAG, "UPDATE_VIEW lat=$lat lng=$lng zoom=$zoomLevel")
        wv.evaluateJavascript("updateView($lat,$lng,$zoomLevel);", null)
    }

    LaunchedEffect(webViewRef.value, points) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        if (points.isEmpty()) return@LaunchedEffect
        val wpArray = points.joinToString(",") { "[${it.lat},${it.lng}]" }
        Log.d(ROUTE_TAG, "SET_WAYPOINTS count=${points.size}")
        wv.evaluateJavascript("setWaypoints([$wpArray]);", null)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val initLat = center?.latitude ?: 16.0544
            val initLng = center?.longitude ?: 108.2022
            Log.d(ROUTE_TAG, "WEBVIEW_FACTORY lat=$initLat lng=$initLng zoom=$zoomLevel")
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        Log.d(ROUTE_TAG, "JS[${msg.messageLevel()}] ${msg.message()} @ ${msg.sourceId()}:${msg.lineNumber()}")
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d(ROUTE_TAG, "PAGE_LOADED url=$url")
                        webViewRef.value = view
                    }
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        Log.e(ROUTE_TAG, "RESOURCE_ERROR url=${request?.url} code=${error?.errorCode} desc=${error?.description}")
                    }
                }
                // Local leaflet assets + CDN for LRM (LRM needs internet for OSRM routing anyway)
                loadDataWithBaseURL(
                    "file:///android_asset/",
                    buildRoutingHtml(points, initLat, initLng, zoomLevel.toDouble(), localLeaflet = true),
                    "text/html", "UTF-8", null
                )
            }
        },
        onRelease = { webView ->
            Log.d(ROUTE_TAG, "WEBVIEW_RELEASE")
            webViewRef.value = null
            webView.stopLoading()
            webView.destroy()
        }
    )
}
