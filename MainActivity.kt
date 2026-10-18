package pk.cinejoy.tv

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val HOME_URL = "https://cinejoy.pk/"
        private const val HOST = "cinejoy.pk"
    }

    private lateinit var root: FrameLayout
    private lateinit var webView: WebView
    private lateinit var cursor: CursorView

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private val density by lazy { resources.displayMetrics.density }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = FrameLayout(this).apply { setBackgroundColor(0xFF000000.toInt()) }
        webView = WebView(this)
        cursor = CursorView(this)

        root.addView(webView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        root.addView(cursor, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContentView(root)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Keep navigation inside the app for the site itself; block random external redirects/popups.
                val host = request.url.host ?: return true
                return !(host == HOST || host.endsWith(".$HOST"))
                        && request.isForMainFrame && request.hasGesture().not()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                if (customView != null) { callback.onCustomViewHidden(); return }
                customView = view
                customViewCallback = callback
                webView.visibility = View.GONE
                cursor.visibility = View.GONE
                root.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }

            override fun onHideCustomView() {
                customView?.let { root.removeView(it) }
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
                webView.visibility = View.VISIBLE
                cursor.visibility = View.VISIBLE
                webView.requestFocus()
            }
        }

        // Start the pointer in the middle of the screen once we know the size.
        root.post {
            cursor.cursorX = root.width / 2f
            cursor.cursorY = root.height / 2f
            cursor.invalidate()
        }

        if (savedInstanceState == null) webView.loadUrl(HOME_URL) else webView.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onPause() { super.onPause(); webView.onPause() }
    override fun onResume() { super.onResume(); webView.onResume() }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    // ---- Remote control handling -------------------------------------------------------------

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode

        // BACK: close fullscreen video -> go back in history -> exit.
        if (code == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP) {
                when {
                    customView != null -> webView.webChromeClient?.onHideCustomView()
                    webView.canGoBack() -> webView.goBack()
                    else -> finish()
                }
            }
            return true
        }

        // While a video is fullscreen, let the player handle every other key.
        if (customView != null) return super.dispatchKeyEvent(event)

        when (code) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (event.action == KeyEvent.ACTION_DOWN) moveCursor(code, event.repeatCount)
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) click()
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                if (event.action == KeyEvent.ACTION_UP) webView.loadUrl(HOME_URL)
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun moveCursor(keyCode: Int, repeat: Int) {
        val step = (10f + minOf(repeat, 25) * 2.5f) * density
        var x = cursor.cursorX
        var y = cursor.cursorY
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> x -= step
            KeyEvent.KEYCODE_DPAD_RIGHT -> x += step
            KeyEvent.KEYCODE_DPAD_UP -> y -= step
            KeyEvent.KEYCODE_DPAD_DOWN -> y += step
        }

        val w = root.width.toFloat()
        val h = root.height.toFloat()
        val edge = 48f * density

        // Pushing the pointer against the top/bottom edge scrolls the page.
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && y > h - edge) webView.scrollBy(0, step.toInt())
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && y < edge) webView.scrollBy(0, -step.toInt())
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && x > w - edge) webView.scrollBy(step.toInt(), 0)
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && x < edge) webView.scrollBy(-step.toInt(), 0)

        cursor.cursorX = x.coerceIn(0f, w - 1)
        cursor.cursorY = y.coerceIn(0f, h - 1)
        cursor.invalidate()
    }

    private fun click() {
        val x = cursor.cursorX
        val y = cursor.cursorY
        val now = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0)
        val up = MotionEvent.obtain(now, now + 60, MotionEvent.ACTION_UP, x, y, 0)
        webView.dispatchTouchEvent(down)
        webView.dispatchTouchEvent(up)
        down.recycle()
        up.recycle()
    }
}
