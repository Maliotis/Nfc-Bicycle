package com.example.nfc_.helpers

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.nfc.NfcAdapter
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.example.nfc_.NFC_ANIMATE_IMG
import com.example.nfc_.NFC_ANIMATE_IMG_DARK_THEME
import com.example.nfc_.activities.MainActivity
import com.example.nfc_.activities.nfcAdapter
import kotlinx.coroutines.*
import org.jetbrains.anko.doAsync


var ctx: MainActivity? = null
var cardView: CardView? = null
var cancelButton: Button? = null

fun rentButton(view: ArrayList<View>, context: MainActivity) {
    ctx = context
    val destWidth = dpToPixels(250f, ctx!!)
    val destHeight = dpToPixels(350f, ctx!!)
    //Expand the cardView

    //TODO enable foreground NFC when the button is pressed
    nfcAdapter?.let {
        NFCUtil.enableNFCInForeground(
            it,
            context as Activity,
            context.javaClass
        )
    }

    val v = view[0]
    var nfcAnimateView = createLottieAnimationView(context)

    //check the casting
    cardView = v.parent as CardView

    val relativeLayout = createRelativeLayout(context)

    cancelButton = createCancelButton(context, nfcAnimateView)

    relativeLayout.addView(nfcAnimateView)
    relativeLayout.addView(cancelButton)

    cardView?.addView(relativeLayout)

    val layoutParams = cardView!!.layoutParams

    val cardViewWidth = cardView!!.measuredWidth
    val cardViewHeight = cardView!!.measuredHeight

    val animWidth = valueAnimatorCardViewWidth(
        cardViewWidth,
        destWidth,
        layoutParams
    )

    val animHeight = valueAnimatorCardViewHeight(
        cardViewHeight,
        destHeight,
        layoutParams
    )

    val animElevation = valueAnimatorCardViewElevation(12f)

    val cardViewX = cardView!!.x
    val cardViewY = cardView!!.y

    val endX = calculate("x",destWidth, ctx!!)
    val animX = valueAnimatorCardViewX(
        cardViewX,
        endX
    )

    val endY = calculate("y", destHeight, ctx!!)
    val animY = valueAnimatorCardViewY(
        cardViewY,
        endY
    )


    //Don't fade out text it bugs
    v.alpha = 0f
    v.isClickable = false

    val fadeNfcLottieAnimator = ObjectAnimator.ofFloat(nfcAnimateView, "alpha", 1f)

    val set = AnimatorSet()
    set.let {
        it.duration = 500
        it.interpolator = FastOutSlowInInterpolator()
        it.playTogether(animX, animY, animWidth, animHeight, animElevation, fadeNfcLottieAnimator)
        it.start()
    }

    cancelButton!!.setOnClickListener {
        val animaWidth = valueAnimatorCardViewWidth(
            destWidth,
            cardViewWidth,
            layoutParams!!
        )
        val animaHeight = valueAnimatorCardViewHeight(
            destHeight,
            cardViewHeight,
            layoutParams
        )
        val animaEle = valueAnimatorCardViewElevation(5f)

        val animaX = valueAnimatorCardViewX(
            cardView!!.x,
            cardViewX
        )

        val animaY = valueAnimatorCardViewY(
            cardView!!.y,
            cardViewY
        )

        val fadeButtonAnimator1 = ObjectAnimator.ofFloat(v, "alpha", 1f)

        val fadeNfcLottieAnimator1 = ObjectAnimator.ofFloat(nfcAnimateView, "alpha", 0f)

        val cancelButtonAnimator = ObjectAnimator.ofFloat(it, "alpha", 0f)

        val set = AnimatorSet()
        set.let {
            it.duration = 500
            it.interpolator = FastOutSlowInInterpolator()
            it.playTogether(animaX, animaY, animaEle, animaHeight, animaWidth, fadeButtonAnimator1, fadeNfcLottieAnimator1, cancelButtonAnimator)
            it.start()
        }
        set.addListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                cardView?.removeView(relativeLayout)
            }

        })
        v.isClickable = true
        nfcAdapter?.let {
            GlobalScope.launch(Dispatchers.Default) {
                disable(it, context as MainActivity)
            }

        }
    }

    //TODO: create programmatically the animated image
    // bring it to the center of the cardView
    // display an exit button on the right bottom of the cardView
    // NFC animation is from @Jin Qu / LottieFiles

    //TODO: fade in the nfc scan animated image
}

suspend fun disable(it: NfcAdapter, context: MainActivity) {
    delay(200)
    NFCUtil.disableNFCInForeground(it, context)
}

private fun valueAnimatorCardViewElevation(end: Float): ValueAnimator? {
    val animElevation = ValueAnimator.ofFloat(cardView!!.elevation, end)
    animElevation.addUpdateListener {
        val value: Float = it.animatedValue as Float
        cardView?.elevation = value
    }
    animElevation.interpolator = OvershootInterpolator()
    return animElevation
}

private fun valueAnimatorCardViewHeight(
    cardViewHeight: Int,
    end: Int,
    layoutParams: ViewGroup.LayoutParams
): ValueAnimator? {
    val animHeight = ValueAnimator.ofInt(cardViewHeight, end)
    animHeight.addUpdateListener {
        val value: Int = it.animatedValue as Int
        layoutParams.height = value
        cardView?.layoutParams = layoutParams
    }
    animHeight.interpolator = OvershootInterpolator()
    return animHeight
}

private fun valueAnimatorCardViewWidth(
    cardViewWidth: Int,
    end: Int,
    layoutParams: ViewGroup.LayoutParams
): ValueAnimator? {
    val animWidth = ValueAnimator.ofInt(cardViewWidth, end)
    animWidth.addUpdateListener {
        val value: Int = it.animatedValue as Int
        layoutParams.width = value
        cardView?.layoutParams = layoutParams
    }
    animWidth.interpolator = OvershootInterpolator()
    return animWidth
}

private fun valueAnimatorCardViewX(cardViewX: Float, endX: Float): ValueAnimator {
    val animX = ValueAnimator.ofFloat(cardViewX, endX)
    animX.addUpdateListener {
        val value: Float = it.animatedValue as Float
        cardView?.x = value
    }
    return animX
}

private fun valueAnimatorCardViewY(cardViewY: Float, endY: Float): ValueAnimator {
    val animY = ValueAnimator.ofFloat(cardViewY, endY)
    animY.addUpdateListener {
        val value: Float = it.animatedValue as Float
        cardView?.y = value
    }
    return animY
}

fun calculate(xOrY: String, cardViewMeasurement: Int, context: Activity): Float {
    val displayMetrics = DisplayMetrics()
    context.windowManager.defaultDisplay.getMetrics(displayMetrics)
    val height = displayMetrics.heightPixels
    val width = displayMetrics.widthPixels

    var result = 0
    if (xOrY == "x") {
        result = (width - cardViewMeasurement) / 2
    } else if (xOrY == "y") {
        result = (height - cardViewMeasurement) / 2
    }

    return result.toFloat()
}

fun createCancelButton(context: Context, view: View): Button {
    val button = Button(context)
    var rlp: RelativeLayout.LayoutParams
    button.apply {
        id = View.generateViewId()
        text = "Cancel"
        setTextColor(resources.getColor(android.R.color.holo_purple))
        setBackgroundColor(Color.TRANSPARENT)
        rlp =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        //rlp.addRule(RelativeLayout.BELOW, view.id)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        layoutParams = rlp
    }

    return button
}

private fun createRelativeLayout(context: Context): RelativeLayout {
    val relativeLayout = RelativeLayout(context)
    relativeLayout.id = View.generateViewId()
    relativeLayout.layoutParams =
        RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    return relativeLayout
}

private fun createLottieAnimationView(context: Context): LottieAnimationView {
    var view = LottieAnimationView(context)
    view.apply {
        id = View.generateViewId()
        if (ctx!!.isDarkTheme()) {
            setAnimation(NFC_ANIMATE_IMG_DARK_THEME)
            val filter = SimpleColorFilter(resources.getColor(android.R.color.holo_purple))
            val keyPath = KeyPath("**")
            val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)
            addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
        } else {
            setAnimation(NFC_ANIMATE_IMG)
        }
        playAnimation()
        progress = 0.1F
        repeatCount = LottieDrawable.INFINITE
        setMinProgress(0.1F)
        scale = 0.7f
        speed = 1.1f
        alpha = 0f
    }
    return view
}