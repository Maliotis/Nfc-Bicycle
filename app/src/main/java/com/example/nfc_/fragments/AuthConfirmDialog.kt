package com.example.nfc_.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import com.example.nfc_.webview_clients.MyWebViewClient

/**
 * Created by Your name on 2019-07-13.
 */

class AuthConfirmDialog(context: Context, url: String): DialogFragment() {
    val ctx = context
    private var url = url
    private lateinit var webView: WebView
    lateinit var relativeLayout: RelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        relativeLayout = RelativeLayout(ctx)
        webView = WebView(ctx)
        webView.settings.useWideViewPort = true
        webView.webViewClient = MyWebViewClient(activity!!)
        webView.loadUrl(url)
        val p =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        webView.layoutParams = p
        relativeLayout.addView(webView)
        return relativeLayout
    }
}