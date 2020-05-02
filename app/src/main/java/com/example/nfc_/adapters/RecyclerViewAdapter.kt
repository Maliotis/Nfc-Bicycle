package com.example.nfc_.adapters

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.ViewUtils
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import com.example.nfc_.R
import com.example.nfc_.activities.MainActivity
import com.example.nfc_.fragments.PaymentFragment
import com.example.nfc_.helpers.PopUpLayout
import com.example.nfc_.helpers.dpToPixels
import com.google.gson.Gson
import com.stripe.android.model.Card
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur


var context:Context? = null
val TAG = RecyclerViewAdapter::class.java.simpleName

class RecyclerViewAdapter(val dataSet: Array<Card>,
                          private val ctx: MainActivity): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    var handler: Handler = Handler()
    var animation: ValueAnimator? = null
    var animation1: ValueAnimator? = null
    var set: AnimatorSet? = null
    val viewHolderList: MutableList<ViewHolder> = mutableListOf()

    var paymentFragment: PaymentFragment? = null

    init {
        setHasStableIds(true)
        context = ctx

    }

    private fun runnable(rootLayout: View): Runnable = Runnable {

        animation = ValueAnimator.ofFloat(1f, 0.9f)
        animation?.duration = 300
        animation?.interpolator = DecelerateInterpolator()
        animation?.addUpdateListener {
            val value =  it.animatedValue as Float
            rootLayout.scaleY = value
            rootLayout.scaleX = value
        }
        animation?.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) = Unit
            override fun onAnimationEnd(animation: Animator?) {
                rootLayout.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                // show menu to delete card
                if (paymentFragment?.popupWindowPF == null) {
                    val location = IntArray(2)
                    rootLayout.getLocationOnScreen(location)
                    val x = location[0]
                    val y = location[1]
                    val width = rootLayout.width//dpToPixels(290f, ctx)
                    val height = rootLayout.height//dpToPixels(170f, ctx)//510//rootLayout.height
                    Log.d(
                        TAG,
                        "onAnimationEnd: rootLayout: x = $x, y = $y, width = $width, height = $height"
                    )

                    val popupX = x + width - dpToPixels(260f, ctx)
                    val popupY = y + height - dpToPixels(10f, ctx)
                    Log.d(TAG, "onAnimationEnd: popup: x = $popupX, y = $popupY")

                    val popupView = setUpBlurView()

                    popupView.layoutParams =
                        RelativeLayout.LayoutParams(dpToPixels(260f, ctx), WRAP_CONTENT)
                    popupView.x = popupX.toFloat()
                    popupView.y = popupY.toFloat()
                    popupView.alpha = 0f
                    popupView.pivotX = dpToPixels(260f, ctx).toFloat()
                    popupView.pivotY = 0f
                    popupView.scaleX = 0.4f
                    popupView.scaleY = 0.4f
                    paymentFragment?.addPopupView(popupView)
                }

            }

            override fun onAnimationCancel(animation: Animator?) = Unit
            override fun onAnimationStart(animation: Animator?) = Unit
        })

        animation1 = ValueAnimator.ofFloat(0.9f, 1f)
        animation1?.duration = 150
        animation1?.interpolator = AccelerateInterpolator()
        animation1?.addUpdateListener {
            val value = it.animatedValue as Float
            rootLayout.scaleY = value
            rootLayout.scaleX = value
        }


        set = AnimatorSet()
        set?.playSequentially(animation, animation1!!)
        set?.start()

    }

    private fun setUpBlurView(): PopUpLayout {
        val popupView =
            LayoutInflater.from(ctx).inflate(R.layout.pop_layout_window, null) as PopUpLayout
        val blurView: BlurView = popupView.findViewById(R.id.blurView)
        val decorView = paymentFragment?.activity?.window?.decorView
        val rootView: ViewGroup? = decorView?.findViewById(android.R.id.content)
        val windowBackground = decorView?.background
        blurView.setupWith(rootView!!)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(ctx))
            .setBlurRadius(25f)
            .setHasFixedTransformationMatrix(true)

        blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurView.clipToOutline = true
        return popupView
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootLayout = LayoutInflater.from(parent.context).inflate(R.layout.credit_card_layout, parent,false)
        val creditCardView = rootLayout.findViewById<CardView>(R.id.creditCardView)
        val selectedTextView = rootLayout.findViewById<TextView>(R.id.selectedCard)
        val creditCardLast4: TextView = rootLayout.findViewById(R.id.creditCardLast4)
        val brandImageView: ImageView = rootLayout.findViewById(R.id.creditCardBrand)
        val creditCardHolderName: TextView = rootLayout.findViewById(R.id.creditCardHolderName)
        val creditCardExpDate: TextView = rootLayout.findViewById(R.id.creditCardExpDate)
        rootLayout.isHapticFeedbackEnabled = true

        rootLayout.setOnLongClickListener {
            Log.d(TAG, "onCreateViewHolder: setOnLongClickListener called")
            handler = Handler()
            handler.post(runnable(creditCardView))
            true
        }


        val viewHolder =  ViewHolder(
            rootLayout = rootLayout,
            selectedTextView = selectedTextView,
            creditCardLast4 = creditCardLast4,
            brandImageView = brandImageView,
            creditCardHolderName = creditCardHolderName,
            creditCardExpDate = creditCardExpDate,
            recyclerViewAdapter = this
        )

        viewHolderList.add(viewHolder)
        return viewHolder
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.brandImageView.setImageDrawable(getDrawableFromPosition(holder, dataSet[position].brand))
        holder.creditCardLast4.text = dataSet[position].last4
        holder.creditCardHolderName.text = dataSet[position].name
        holder.creditCardExpDate.text = "${dataSet[position].expMonth}/${dataSet[position].expYear}"

        if (position != getRowForCheckedCheckBox()) {
            holder.selectedTextView.alpha = 0f
        } else {
            holder.selectedTextView.alpha = 1f
        }
        // TODO: save card
    }

    private fun getDrawableFromPosition(holder :ViewHolder, brand: String?): Drawable? {
        Log.d(TAG, "getDrawableFromPosition: brand = $brand")
        if (brand?.toLowerCase() == "visa") {
            holder.rootLayout.findViewById<RelativeLayout>(R.id.creditCardRelativeLayout).background = context?.resources?.getDrawable(R.drawable.background_visa, null)
            return context?.resources?.getDrawable(R.drawable.ic_visa_initial_white, null)
        } else if (brand?.toLowerCase() == "mastercard") {
            return context?.resources?.getDrawable(R.drawable.ic_mastercard_initial_white, null)
        }
        return null
    }

    fun saveCardForUse(layoutPosition: Int) {
        Log.d(TAG, "saveCardForUse: caleld")
        val editor = context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
        val set = editor?.getStringSet("cards", mutableSetOf())?.toTypedArray()
        val string = set?.get(layoutPosition)
        val gson = Gson()
        val card: Card? = gson.fromJson(string, Card::class.java)
        card?.last4?.run {
                context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)?.edit {
                    putString("card_to_use", string)
                    putInt("card_to_use_row", layoutPosition)
                }

        }

    }

    // Use that to set the correct card
    private fun getRowForCheckedCheckBox(): Int? {
        val row = context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
            ?.getInt("card_to_use_row", -1)
        return row
    }

    // VIEW-HOLDER

    class ViewHolder(val rootLayout: View,
                     val selectedTextView: TextView,
                     val creditCardLast4: TextView,
                     val brandImageView: ImageView,
                     val creditCardHolderName: TextView,
                     val creditCardExpDate: TextView,
                     val recyclerViewAdapter: RecyclerViewAdapter) : RecyclerView.ViewHolder(rootLayout) {


        init {
            this.layoutPosition
            rootLayout.setOnClickListener {
                val index = getRowForCheckedCheckBox() ?: -1
                if (index >= 0)
                    recyclerViewAdapter.viewHolderList.get(index).selectedTextView.alpha = 0f
                saveCardForUse(this.layoutPosition)
                selectedTextView.animate().alpha(1f)
            }
            rootLayout.tag = this.layoutPosition
        }

        private fun deleteCardToUse(layoutPosition: Int) {
            Log.d(TAG, "deleteCardToUse: called")
            context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)?.edit {
                putString("card_to_use", "")
                putInt("card_to_use_row", -1)
            }

        }

        fun saveCardForUse(layoutPosition: Int) {
            Log.d(TAG, "saveCardForUse: called")
            val editor = context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
            val set = editor?.getStringSet("cards", mutableSetOf())?.toTypedArray()
            val string = set?.get(layoutPosition)
            val gson = Gson()
            val card: Card? = gson.fromJson(string, Card::class.java)
            card?.last4?.run {
                context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)?.edit {
                    putString("card_to_use", string)
                    putInt("card_to_use_row", layoutPosition)
                }

            }

        }

        private fun getRowForCheckedCheckBox(): Int? {
            val row = context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
                ?.getInt("card_to_use_row", -1)
            return row
        }

    }

}
