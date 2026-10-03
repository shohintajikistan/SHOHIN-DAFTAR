package tj.shohin.daftar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST = 1001
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        setContentView(webView)

        setupWebView()

        webView.loadUrl("file:///android_asset/index.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {

        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadsImagesAutomatically = true
        settings.javaScriptCanOpenWindowsAutomatically = true

        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {

                val url = request.url.toString()

                if (
                    url.startsWith("http://") ||
                    url.startsWith("https://")
                ) {

                    return try {

                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                        )

                        startActivity(intent)

                        true

                    } catch (e: Exception) {

                        false
                    }
                }

                return false
            }
        }

        webView.webChromeClient =
            object : WebChromeClient() {

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {

                    fileChooserCallback?.onReceiveValue(null)

                    fileChooserCallback = filePathCallback

                    val intent =
                        fileChooserParams?.createIntent()
                            ?: Intent(Intent.ACTION_GET_CONTENT).apply {

                                addCategory(
                                    Intent.CATEGORY_OPENABLE
                                )

                                type = "image/*"
                            }

                    return try {

                        startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST
                        )

                        true

                    } catch (e: Exception) {

                        fileChooserCallback = null

                        Toast.makeText(
                            this@MainActivity,
                            "Не удалось открыть файл",
                            Toast.LENGTH_SHORT
                        ).show()

                        false
                    }
                }
            }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == FILE_CHOOSER_REQUEST) {

            val callback = fileChooserCallback

            fileChooserCallback = null

            if (callback == null) {
                return
            }

            val results =
                if (
                    resultCode == RESULT_OK &&
                    data != null
                ) {

                    val uri = data.data

                    if (uri != null) {
                        arrayOf(uri)
                    } else {
                        null
                    }

                } else {

                    null
                }

            callback.onReceiveValue(results)
        }
    }

    override fun onBackPressed() {

        if (webView.canGoBack()) {

            webView.goBack()

        } else {

            super.onBackPressed()
        }
    }

    override fun onDestroy() {

        webView.stopLoading()
        webView.destroy()

        super.onDestroy()
    }
}