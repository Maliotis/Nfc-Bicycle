package com.example.nfc_.data

import android.graphics.drawable.Drawable


/**
 * Created by petrosmaliotis on 19/04/2020.
 */

data class HistoryTransactionData (
    var imageDrawable: Drawable? = null,
    var lastFour: String? = null,
    var date: String? = null,
    var created: Long? = null,
    var amount: String? = null,
    var receiptUrl: String? = null
) : DataDSL {
    constructor(): this(null, null, null, null, null)
}