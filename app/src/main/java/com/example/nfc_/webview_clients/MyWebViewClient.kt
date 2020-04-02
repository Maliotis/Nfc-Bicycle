package com.example.nfc_.webview_clients

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe

/**
 * Created by Your name on 2019-07-12.
 */

class MyWebViewClient internal constructor(private val activity: Activity) : WebViewClient() {

    val stripe = Stripe(activity, PaymentConfiguration.getInstance().publishableKey)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url: String = request?.url.toString()
        if (url.contains("yourapp")) {
            url.replace("yourapp://post-authentication-return-url?", "")
            showReceiptUrl(url)
        }
        if (url.split(":")[0].equals("yourapp")) {
            val parent = view?.parent as ViewGroup
            Log.i("returnUrl", url)
            //parent.removeView(view)
        } else {
            view?.loadUrl(url)
        }
        //view?.loadUrl(url)
        return true
    }

    private fun showReceiptUrl(url: String) {
        val parameterName = url.split("&")
        var map: HashMap<String, String> = HashMap()
        if (parameterName.size > 1) {
            parameterName.forEach {
                val paramAndValue = it.split("=")
                //Name          ,  Value
                map.put(paramAndValue[0], paramAndValue[1])
            }
        }
        for (mutableEntry in map) {
            if (mutableEntry.key.equals("payment_intent_client_secret")) {
                val run = Runnable {
//                    val retrievePaymentIntentParams = PaymentIntentParams.createRetrievePaymentIntentParams(
//                        mutableEntry.value
//                    )
                    //TODO: execute on background thread
//                    val paymentIntent = stripe.retrievePaymentIntentSynchronous(
//                        retrievePaymentIntentParams
//                    )
//                    Log.i("paymentIntentReceived",paymentIntent?.status.toString())
//                    Log.i("redirectDataUrl",paymentIntent?.redirectData?.url.toString())
                }

                val thread = Thread(run)
                thread.start()

            }
        }
    }

    override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
        webView.loadUrl(url)
        return true
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        Toast.makeText(activity, "Got Error! $error", Toast.LENGTH_SHORT).show()
    }
}