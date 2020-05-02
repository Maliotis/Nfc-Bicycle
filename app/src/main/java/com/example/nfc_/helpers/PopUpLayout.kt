package com.example.nfc_.helpers

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.toBitmap
import com.example.nfc_.R
import com.example.nfc_.activities.isDarkTheme

/**
 * Created by petrosmaliotis on 27/04/2020.
 */
class PopUpLayout @JvmOverloads constructor(context: Context,
                                            attrs: AttributeSet? = null,
                                            defStyle: Int = 0,
                                            defStyleRes: Int = 0) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    val path = Path()
    val paint = Paint()
    val beginOfArrow = dpToPixels(110f, context)
    var myWidth = 0
    private var myHeight: Int = 0

    private val arrowSize = dpToPixels(80f, context)

    init {
        this.setWillNotDraw(false)
    }


    override fun onDraw(canvas: Canvas?) {
        Log.d(TAG, "onDraw: called")
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 1f

        val drawable = context.resources.getDrawable(R.drawable.pop_up_arrow, null)
        if (isDarkTheme)
            drawable.setColorFilter(Color.parseColor("#50383838"), PorterDuff.Mode.MULTIPLY)
        val bitmap = drawable.toBitmap(arrowSize, arrowSize, Bitmap.Config.ARGB_4444)
        val widthMinusBeginOfArrow = width.toFloat() - beginOfArrow
        canvas?.drawBitmap(bitmap, widthMinusBeginOfArrow, -20f, paint)

//        paint.style = Paint.Style.STROKE
//        paint.strokeWidth = 2f
//        paint.color = Color.DKGRAY
//        path.apply {
//            moveTo(0f,70f)
//            val widthMinusBeginOfArrow = width.toFloat() - beginOfArrow
//            lineTo(widthMinusBeginOfArrow, 70f)
//            moveTo(widthMinusBeginOfArrow, 70f)
//            quadTo(widthMinusBeginOfArrow + 30f, 70f , widthMinusBeginOfArrow + 30f, 30f)
//            moveTo(widthMinusBeginOfArrow + 30f, 30f)
//            quadTo(widthMinusBeginOfArrow + 40f, 20f, widthMinusBeginOfArrow + 40f, 30f)
//            moveTo(widthMinusBeginOfArrow + 40f, 30f)
//            quadTo(widthMinusBeginOfArrow + 30f, 70f, widthMinusBeginOfArrow + 40f, 70f)
//            close()
//        }

//        canvas?.drawPath(path, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        myWidth = w
        myHeight = h
    }
}