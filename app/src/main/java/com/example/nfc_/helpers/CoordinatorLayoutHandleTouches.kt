package com.example.nfc_.helpers

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * Created by petrosmaliotis on 30/04/2020.
 */

class CoordinatorLayoutHandleTouches @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0)
    : CoordinatorLayout(context, attributeSet, defStyleAttr) {

    var methodToRunOnTouchParent: () -> Unit = {}

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        methodToRunOnTouchParent()
        return super.dispatchTouchEvent(ev)
    }
}