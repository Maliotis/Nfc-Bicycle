package com.example.nfc_.helpers

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat.getTranslationY
import androidx.core.view.ViewCompat.setTranslationY
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import kotlin.math.min


/**
 * Created by petrosmaliotis on 21/03/2020.
 */
class MoveUpwardBehavior: CoordinatorLayout.Behavior<View> {

    constructor (): super() {

    }

    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs) {
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is SnackbarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {

        val translationY = min(0f, dependency.translationY - dependency.height)
        child.translationY = translationY
        Log.d(TAG, "onDependentViewChanged: translationY = $translationY")
        return true
    }

    //you need this when you swipe the snackbar(thanx to ubuntudroid's comment)
    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        child.animate().translationY(0f).start()
    }
}