package com.example.nfc_.helpers

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue




/**
 * Created by petrosmaliotis on 16/03/2020.
 */

fun <T> ifElse(condition: Boolean, primaryResult: T, secondaryResult: T): T {
    return if (condition) {
        primaryResult
    } else {
        secondaryResult
    }
}

fun dpToPixels(dp: Float, context: Context): Int {
    val r: Resources = context.resources
    val px = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        r.displayMetrics
    )
    return px.toInt()
}