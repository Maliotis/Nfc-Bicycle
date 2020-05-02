package com.example.nfc_.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.example.nfc_.BIKE_ANIM_IMG
import com.example.nfc_.R
import com.example.nfc_.activities.MainActivity
import com.example.nfc_.activities.database
import com.example.nfc_.adapters.HistoryRecyclerViewAdapter
import com.example.nfc_.buildPathEvents
import com.example.nfc_.data.HistoryTransactionData
import com.example.nfc_.data.constructObject
import com.example.nfc_.helpers.Timer
import com.example.nfc_.helpers.dpToPixels
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

/**
 * Created by petrosmaliotis on 17/04/2020.
 */

private val TAG = HistoryFragment::class.java.simpleName
class HistoryFragment(private val mainActivity: MainActivity): Fragment(), AppBarLayout.OnOffsetChangedListener {

    // Views
    private lateinit var timeCard: View
    private lateinit var distanceCard: View
    private lateinit var amountCard: View
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewTimeValueItem: TextView
    private lateinit var textViewAmountValueItem: TextView
    private lateinit var transactionCardView: CardView
    private lateinit var collapsedRelativeButton: Button
    private lateinit var nestedRelativeLayout: RelativeLayout
    private lateinit var loadingLottieAnimationView: LottieAnimationView

    // Attributes
    private val marginTopForNestedView = dpToPixels(18f, mainActivity)
    private lateinit var layoutManager: LinearLayoutManager

    // Adapter
    private var recyclerViewAdapter: HistoryRecyclerViewAdapter? = null

    // Animate Helpers
    private var firstTimeEncounter: Boolean = true
    private var moveUpwardsTD: Float = 0f
    private var moveUpwardsDM: Float = 0f
    private var origElevation = 4f
    private var stackOffset: Int = 0
    private var visible: Boolean = false

    // Observable
    private var timeObservable: Observable<Long>? = null

    // Gson
    private var gson: Gson? = null
        set(value) {
            if (field == null )
                field = value
        }

    // Data for RecyclerView Adapter
    private var adapterData: MutableList<HistoryTransactionData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        stackOffset = dpToPixels(10f, mainActivity)
        val view = inflater.inflate(R.layout.activity_history, container, false)
        timeCard = view.findViewById(R.id.timeCard)
        distanceCard = view.findViewById(R.id.distanceCard)
        amountCard = view.findViewById(R.id.amountCard)
        appBarLayout = view.findViewById(R.id.appBarLayoutActivity)
        relativeLayout = view.findViewById(R.id.cardViewHolderRelativeLayout)
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        textViewTimeValueItem = view.findViewById(R.id.csTimeValue)
        textViewAmountValueItem = view.findViewById(R.id.csAmountValue)
        transactionCardView = view.findViewById(R.id.transactionCardView)
        collapsedRelativeButton = view.findViewById(R.id.currentSessionButton)
        nestedRelativeLayout = view.findViewById(R.id.nestedRelativeLayout)
        loadingLottieAnimationView = view.findViewById(R.id.bikeLottie)

        transactionCardView.alpha = 0f
        collapsedRelativeButton.alpha = 0f

        setRecyclerViewAttributes()
        setLottieAnimation()

        appBarLayout.addOnOffsetChangedListener(this)
        timeCard.elevation = origElevation
        distanceCard.elevation = origElevation
        amountCard.elevation = origElevation


        distanceCard.tag = origElevation
        amountCard.tag = origElevation

        setTimerObservable()
        setCurrentSessionButtonClickListener()
        queryPagination()

        return view
    }

    private fun setLottieAnimation() {
        val filter = SimpleColorFilter(Color.WHITE)
        val keyPath = KeyPath("**")
        val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)
        loadingLottieAnimationView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
        loadingLottieAnimationView.apply {
            setAnimation(BIKE_ANIM_IMG)
            playAnimation()

            repeatCount = LottieDrawable.INFINITE
        }
    }

    private fun setCurrentSessionButtonClickListener() {
        val linearSmoothScroller: LinearSmoothScroller =
            object : LinearSmoothScroller(recyclerView.context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return 50f / displayMetrics.densityDpi
                }
            }
        linearSmoothScroller.targetPosition = 0
        Log.d(TAG, "setCurrentSessionButtonClickListener: itemCount = ${recyclerViewAdapter?.itemCount}")
        collapsedRelativeButton.setOnClickListener {
            // scroll everything back to top
            appBarLayout.setExpanded(true, true)
            //recyclerView.scrollToPosition(0)
            linearSmoothScroller.targetPosition = 0
            layoutManager.startSmoothScroll(linearSmoothScroller)
        }
    }

    /**
     * Grab the offset from the AppBarLayout changes position when user scrolls
     */
    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = offset.absoluteValue / maxScroll.toFloat()

        // reset all views
        if (percentage == 0f)
            resetAllViews()


        // animate the cards using the percentage and fade in
        if (percentage > 0f && percentage <= 0.20) {
            if (firstTimeEncounter) {
                moveUpwardsTD = distanceCard.y - timeCard.y - stackOffset
                moveUpwardsDM = amountCard.y - timeCard.y - 2 * stackOffset
                firstTimeEncounter = false
            }

            animateViews(timeCard, distanceCard, moveUpwardsTD, percentage, 4f)
            animateViews(distanceCard, amountCard, moveUpwardsDM, percentage, 8f)

            if (!visible) {
                fadeAllViewsWithValue(1f)
                // add that to onCompleteAnimation of the fadeAllViewsWithValue
                collapsedRelativeButton.animate().alpha(0f).duration = 100
                if (nestedRelativeLayout.marginTop > 0) {
                    //animateNestedRelativeLayout(-marginTopForNestedView)
                }
            }

        }
        // fade out the cards
        else if (percentage > 0.20) {
            if (visible) {
                fadeAllViewsWithValue(0f)
                // add that to onCompleteAnimation of the fadeAllViewsWithValue
                collapsedRelativeButton.animate().alpha(1f).duration = 200
                //animateNestedRelativeLayout(marginTopForNestedView)
            }
        }
    }

    private fun animateNestedRelativeLayout(marginTop: Int) {
        val animator = ValueAnimator.ofInt(nestedRelativeLayout.marginTop, nestedRelativeLayout.marginTop + marginTop)
        animator.duration = 150
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            val layoutParams = nestedRelativeLayout.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.setMargins(0, value, 0, 0)
            nestedRelativeLayout.layoutParams = layoutParams
        }
        animator.start()

    }

    /**
     * Set all views to the original position
     */
    private fun resetAllViews() {
        timeCard.animate()
            .setInterpolator(DecelerateInterpolator())
            .translationY(0f)
            .duration = 100
        distanceCard.animate()
            .setInterpolator(DecelerateInterpolator())
            .translationY(0f)
            .duration = 100
        amountCard.animate()
            .setInterpolator(DecelerateInterpolator())
            .translationY(0f)
            .duration = 100
    }

    /**
     * Fade the relative layout that contains all the card views
     */
    private fun fadeAllViewsWithValue(value: Float) {
        visible = value == 1f
        relativeLayout.animate()
            .alpha(value)
            .duration = 250
    }

    /**
     * Shrink and move views to look like the stack one top of another
     */
    private fun animateViews(shrinkView: View, selectedView: View, moveUpwards: Float, percentage: Float, scaleFactor: Float) {
        // Set scale
        val scale = percentage / scaleFactor
        shrinkView.scaleX = 1 - scale
        shrinkView.scaleY = 1 - scale

        // Set elevation
        val elevate = percentage * scaleFactor
        selectedView.elevation = (selectedView.tag as Float) + elevate

        // Set translationY
        val translationY = percentage / (0.2f / moveUpwards)
        selectedView.translationY = - translationY
    }

    private fun setTimerObservable() {
        if (Timer.instance()?.running == true) {
            // then get observable
            timeObservable = Timer.instance()?.getTimeObservable(1,1, TimeUnit.SECONDS)
            val d = timeObservable
                ?.subscribeOn(AndroidSchedulers.mainThread())
                ?.subscribe ({
                    //Log.d(TAG, "setTimerObservable: thread = ${Thread.currentThread().name}")
                    val time = Timer.instance()?.getElapsedTimeSmart()!!
                    Log.d(TAG, "setTimerObservable: time = $time")
                    setTimeValue(time)
                    val amount = mainActivity.calculateAmount(50.0)
                    setAmountValue("£ ${amount/100f}")
                }, {
                    it.printStackTrace()
                })
        }
    }

    // Set Values

    private fun setTimeValue(value: String) {
        var valueToSet = value
        if (value.isEmpty())
            valueToSet = "--"

        textViewTimeValueItem.text = valueToSet
    }

    fun setDistanceValue(value: String) {
        var valueToSet = value
        if (value.isEmpty())
            valueToSet = "--"

        val textView: TextView = distanceCard.findViewById(R.id.csValueDistance)
        textView.text = valueToSet
    }

    private fun setAmountValue(value: String) {
        var valueToSet = value
        if (value.isEmpty())
            valueToSet = "--"

        textViewAmountValueItem.text = valueToSet
    }

    // recycler view attributes
    private fun setRecyclerViewAttributes() {
        layoutManager = LinearLayoutManager(mainActivity)
        val itemDecorator = DividerItemDecoration(mainActivity, DividerItemDecoration.HORIZONTAL)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = HistoryRecyclerViewAdapter(mutableListOf<HistoryTransactionData>())
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.setHasFixedSize(true)
        //recyclerView.addItemDecoration(itemDecorator)

    }

    // start query pagination
    private fun queryPagination() {
        database.child(buildPathEvents()).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                createHistoryTransactionDataObservable(dataSnapshot)
            }

        })

    }

    private fun createHistoryTransactionDataObservable(dataSnapshot: DataSnapshot) {
        val d = Observable.create<MutableList<HistoryTransactionData>> { emitter ->
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                dataSnapshot.children.forEach { snapshot ->
                    val historyData = createHistoryData(snapshot)
                    if (historyData != null)
                        adapterData.add(historyData)

                }
                adapterData.sortBy {
                    it.created
                }
                val comparator = compareByDescending<HistoryTransactionData> { it.created }
                adapterData.sortWith(comparator)
                emitter.onNext(adapterData)
            }
            emitter.onComplete()

        }.subscribeOn(Schedulers.io())
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ historyDataList ->

                recyclerViewAdapter?.data = historyDataList//.subList(0, 10)
                loadingLottieAnimationView.animate().alpha(0f).setListener(object: Animator.AnimatorListener{
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        recyclerViewAdapter?.notifyDataSetChanged()
                        transactionCardView.animate().alpha(1f).duration = 250
                        loadingLottieAnimationView.cancelAnimation()
                    }

                }).duration = 300

            },{
                it.printStackTrace()
            })
    }

    private fun createHistoryData(snapshot: DataSnapshot): HistoryTransactionData? {
        try {
            gson = Gson()
            val hashMap = snapshot.value as HashMap<String, Any>
            val data = (hashMap["data"] as java.util.HashMap<*, *>)

            val charges =
                (data["object"] as java.util.HashMap<*, *>)["charges"] as java.util.HashMap<*, *>

            val map0 =
                (charges["data"] as ArrayList<*>)[0] as java.util.HashMap<*, *>

            val receiptUrl = map0["receipt_url"] as String
            val amount = map0["amount"] as Long
            val created = map0["created"] as Long

            val paymentMethodDetails =
                map0["payment_method_details"] as HashMap<*, *>
            val card = paymentMethodDetails["card"] as HashMap<*, *>
            val brand = card["brand"] as String
            val last4 = card["last4"] as String

            val historyTransactionData = constructObject<HistoryTransactionData> {
                this.amount = "£ " + (amount.toDouble() / 100.0).toString()
                this.receiptUrl = receiptUrl
                this.lastFour = last4

                val date1 = Date(created * 1000L)
                val jdf = SimpleDateFormat("d MMMM yyyy")
                this.date = jdf.format(date1)
                this.created = created
                Log.d(TAG, "onChildAdded: date = ${this.date}")

                val bitmap = getBrandBitmap(brand)
                this.imageDrawable = bitmap
            }
            // return historyTransactionData
            return historyTransactionData

        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        } catch (e: TypeCastException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getBrandBitmap(brand: String?): Drawable? {
        return when (brand) {
            "visa" -> {
                getVisaBitmap()
            }
            "mastercard" -> {
                getMasterCardBitmap()
            }
            "american express" -> {
                getAmexBitmap()
            }
            "discover" -> {
                getDiscoverBitmap()
            }
            else -> {
                null
            }
        }
    }

    private fun getDiscoverBitmap(): Drawable {
        return resources.getDrawable(R.drawable.ic_iconfinder_discover_initial, null)
    }

    private fun getAmexBitmap(): Drawable {
        return resources.getDrawable(R.drawable.ic_iconfinder_american_express_initial, null)
    }

    private fun getMasterCardBitmap(): Drawable {
        if (mainActivity.isDarkTheme()) {
            return resources.getDrawable(R.drawable.ic_mastercard_initial_white, null)
        }
        return resources.getDrawable(R.drawable.ic_iconfinder_master_card_initial, null)
    }

    private fun getVisaBitmap(): Drawable {
        if (mainActivity.isDarkTheme()) {
            return resources.getDrawable(R.drawable.ic_visa_initial_white, null)
        }
        return resources.getDrawable(R.drawable.ic_visa_initial, null)
    }
}

