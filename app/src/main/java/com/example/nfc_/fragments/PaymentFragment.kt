package com.example.nfc_.fragments

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.nfc_.R
import com.example.nfc_.activities.MainActivity
import com.example.nfc_.adapters.RecyclerViewAdapter
import com.example.nfc_.helpers.CoordinatorLayoutHandleTouches
import com.google.gson.Gson
import com.stripe.android.model.*
import org.jetbrains.anko.find
import kotlin.math.absoluteValue


class PaymentFragment(private var mainActivity: MainActivity) : Fragment() {

    private var isShowingBackCard: Boolean = false
    val TAG = PaymentFragment::class.java.canonicalName

    private lateinit var inflater: LayoutInflater
    private lateinit var container: ViewGroup

    lateinit var rootLayoutPF: CoordinatorLayout
    var popupWindowPF: View? = null
    val popupWindowContainer: RelativeLayout by lazy {
        RelativeLayout(mainActivity).apply {
            this.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }


    lateinit var viewPager2: ViewPager2
    lateinit var adapterManager: RecyclerViewAdapter

    var paymentViewId: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        this.inflater = inflater
        this.container = container!!
        val view = getPaymentMethodView()
        rootLayoutPF = view
        paymentViewId = view.id

        //adapterManager.notifyDataSetChanged()

        viewPager2.isFocusable = true

        return view
    }

    fun addPopupView(v: View) {
        Log.d(TAG, "addView: called")
        if (popupWindowPF == null || !rootLayoutPF.contains(popupWindowContainer)) {
            popupWindowContainer.addView(v)
            val rootView = mainActivity.findViewById<ViewGroup>(R.id.drawer_layout)//.getChildAt(0) as ViewGroup
            rootView.addView(popupWindowContainer)
//            rootLayoutPF.addView(popupWindowContainer)
            //rootLayoutPF.addView(v, 0)
            v.animate()
                .alpha(1f)
                .scaleY(1f)
                .scaleX(1f)
                .setDuration(250)
                .interpolator = AccelerateDecelerateInterpolator()

            popupWindowPF = v
            //expandViewHitArea(rootLayoutPF, popupWindowPF!!)
//            popupWindowPF!!.setOnTouchListener { v, event ->
//                Log.d(TAG, "popupWindowPF: onClickListener: called")
//                rootLayoutPF.removeView(v)
//                popupWindowPF = null
//                true
//            }
            popupWindowContainer.setOnClickListener {
                popupWindowPF!!.animate()
                    .alpha(0f)
                    .scaleY(0.4f)
                    .scaleX(0.4f)
                    .setDuration(250)
                    .setListener(object: Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) = Unit
                        override fun onAnimationCancel(animation: Animator?) = Unit
                        override fun onAnimationStart(animation: Animator?) = Unit
                        override fun onAnimationEnd(animation: Animator?) {
                            popupWindowContainer.removeView(popupWindowPF)
                            val rootView = mainActivity.findViewById<ViewGroup>(R.id.drawer_layout)//.getChildAt(0) as ViewGroup
                            rootView.removeView(popupWindowContainer)
//                            rootLayoutPF.removeView(it)
                            popupWindowPF = null
                        }
                    })
                    .interpolator = AccelerateDecelerateInterpolator()

            }
        }
        else Log.e(TAG, "addView: view already has a parent.")


    }

    private fun expandViewHitArea(parent : View, child : View) {
        parent.postDelayed( {
            val parentRect = Rect()
            val childRect = Rect()
            parent.getHitRect(parentRect)
            child.getHitRect(childRect)

            childRect.left -= parentRect.width()
            childRect.top -= parentRect.height()
            childRect.right += parentRect.width()
            childRect.bottom += parentRect.height()

            parent.touchDelegate = TouchDelegate(childRect, child)
        }, 250)
    }
    

    @SuppressLint("ClickableViewAccessibility")
    private fun getPaymentMethodView(): CoordinatorLayout {
        val view = inflater.inflate(R.layout.payment_layout, container, false) as CoordinatorLayout

        viewPager2 = view.findViewById(R.id.viewPager2)!!
        val addButton: Button = view.findViewById(R.id.add_newCard)
        addButton.setOnTouchListener { v, event ->
            Log.d(TAG, "getPaymentMethodView: event.action = ${event.action}")
            when(event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    v?.animate()?.scaleX(0.8f)?.duration = 150
                    v?.animate()?.scaleY(0.8f)?.duration = 150
                }

                MotionEvent.ACTION_MOVE -> {
                    v?.animate()?.scaleX(1f)?.duration = 150
                    v?.animate()?.scaleY(1f)?.duration = 150
                }

                MotionEvent.ACTION_UP -> {
                    v?.animate()?.scaleX(1f)?.duration = 150
                    v?.animate()?.scaleY(1f)?.setListener(object: Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) = Unit
                        override fun onAnimationCancel(animation: Animator?) = Unit
                        override fun onAnimationStart(animation: Animator?) = Unit
                        override fun onAnimationEnd(animation: Animator?) {
                            onAddNewCardClick()
                        }
                    })?.duration = 150

                }
            }
            true
        }

        val dataSet: MutableList<Card> = getDataSetCards()
        var column = getSavedCard()
        adapterManager = RecyclerViewAdapter(dataSet.toTypedArray(), mainActivity!!)
        adapterManager.paymentFragment = this
        Handler().postDelayed({
            viewPager2.setCurrentItem(column, true)
        }, 200)

        viewPager2.apply {
            adapter = adapterManager
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - position.absoluteValue
            Log.d(TAG, "getPaymentMethodView: r = $r")
            page.scaleY = 0.85f + r * 0.15f
        }

        viewPager2.setPageTransformer(compositePageTransformer)

        viewPager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // TODO: save card
                Log.d(TAG, "onPageSelected: position = $position")
                //adapterManager.saveCardForUse(position)
            }
        })

        return view!!
    }


    private fun getDataSetCards(): MutableList<Card> {
        val editor = mainActivity.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
        val array = editor.getStringSet("cards", mutableSetOf()).toTypedArray()
        var returnList: MutableList<Card> = mutableListOf()
        array.forEach {
            val gson = Gson()
            val card: Card = gson.fromJson(it, Card::class.java)
            returnList.add(card)

        }
        return returnList
    }

    private fun getSavedCard(): Int {
        val pref = mainActivity.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
        return pref.getInt("card_to_use_row", 0)
    }

    private fun onAddNewCardClick() {

        // TODO change to call AddCardFragment
        val addCardFragment = AddCardFragment(mainActivity)
        mainActivity.supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_placeholder, addCardFragment)
            .commit()
    }




}