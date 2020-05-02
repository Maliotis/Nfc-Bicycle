package com.example.nfc_.fragments

import android.animation.*
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.example.nfc_.R
import com.example.nfc_.activities.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.stripe.android.model.Card
import com.stripe.android.view.CardNumberEditText
import com.stripe.android.view.ExpiryDateEditText
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

/**
 * Created by petrosmaliotis on 26/04/2020.
 */
class AddCardFragment(private var mainActivity: MainActivity) : Fragment() {

    private var isShowingBackCard: Boolean = false
    val TAG = AddCardFragment::class.java.canonicalName

    // Views
    private lateinit var stripeCardNumberEditText: CardNumberEditText
    private lateinit var creditCardEditHolderName: EditText
    private lateinit var creditCardEditBrand: ImageView
    private lateinit var creditCardEditExpDate: ExpiryDateEditText
    private lateinit var backCardView: RelativeLayout
    private lateinit var frontCardView: RelativeLayout
    private lateinit var creditCardCvc: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageButton

    // Rx
    private var stripeCardNumberPublishSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var creditCardExpDatePublishSubject: PublishSubject<Boolean> = PublishSubject.create()

    private lateinit var validNumberAndExpObservable: Observable<Boolean>

    private lateinit var inflater: LayoutInflater
    private lateinit var container: ViewGroup


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        validNumberAndExpObservable = Observable
            .zip(stripeCardNumberPublishSubject, creditCardExpDatePublishSubject, BiFunction { t1, t2 ->  t1 && t2})

        this.inflater = inflater
        this.container = container!!
        val view = addCardView()
        return view
    }

    private fun addTheNewCard(card: Card) {
        val editor = mainActivity.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
        var dataSet = editor.getStringSet("cards", mutableSetOf())
        if (dataSet == null) dataSet = mutableSetOf() // defensive check
        //create json of card and store it as string
        val gson = Gson()
        val jsonString = gson.toJson(card)
        dataSet.add(jsonString)
        val newEditor = editor.edit()
        newEditor.putStringSet("cards", dataSet)
        newEditor.apply()
    }


    private fun addCardView(): View? {
        val constraintLayout = inflater.inflate(R.layout.credit_card_edit_layout, container, false)
        // will contain the credit_card_layout_front
        val cardViewCreditCard = constraintLayout.findViewById<CardView>(R.id.creditCardView)
        creditCardEditBrand = cardViewCreditCard.findViewById<ImageView>(R.id.creditCardEditBrand)
        stripeCardNumberEditText = cardViewCreditCard.findViewById(R.id.stripeCardNumberEditText)
        creditCardEditHolderName = cardViewCreditCard.findViewById(R.id.creditCardEditHolderName)
        creditCardEditExpDate = cardViewCreditCard.findViewById(R.id.creditCardEditExpDate)
        creditCardCvc = cardViewCreditCard.findViewById(R.id.creditCardBackCVC)
        frontCardView = cardViewCreditCard.findViewById<RelativeLayout>(R.id.includeLayout)
        backCardView = cardViewCreditCard.findViewById<RelativeLayout>(R.id.includeLayoutBack)
        saveButton = constraintLayout.findViewById(R.id.saveCreditCardButton)
        backButton = constraintLayout.findViewById(R.id.backButton)

        stripeCardNumberEditText.setText("4242-4242-4242-4242")

        // if everything is valid present user with the back of the card CVC
        val d = validNumberAndExpObservable.subscribe { isValid ->
            if (isValid) showBackCard(cardViewCreditCard)
        }

        // Disable back card view
        backCardView.isEnabled = false

        if (stripeCardNumberEditText.isCardNumberValid) {
            stripeCardNumberPublishSubject.onNext(true)
        }

        stripeCardNumberEditText.doAfterTextChanged {
            // get card type and update background color and image view icon
            // check if number is correct
            Log.d(TAG, "addCardView: brand = ${stripeCardNumberEditText.cardBrand}")
            changeBackgroundColor(stripeCardNumberEditText.cardBrand)
            changeImageDrawable(stripeCardNumberEditText.cardBrand)

            if (stripeCardNumberEditText.isCardNumberValid) {
                stripeCardNumberPublishSubject.onNext(true)
            }
            isCreditCardValid()
        }

        creditCardEditExpDate.doAfterTextChanged {
            // check if number is correct
            if (creditCardEditExpDate.isDateValid) {
                creditCardExpDatePublishSubject.onNext(true)
            }
            //save state
            isCreditCardValid()
        }

        creditCardEditHolderName.doAfterTextChanged {
            // save state
        }

        creditCardCvc.doAfterTextChanged {
            if (creditCardCvc.text.length == 3) {
                // retire soft keyboard
                // do anim
            }
            isCreditCardValid()
        }

        constraintLayout.setOnClickListener {
            if (!isShowingBackCard) showBackCard(cardViewCreditCard)
            else showFrontCard(cardViewCreditCard)
        }

        saveButton.setOnClickListener(getOkClickListener())
        backButton.setOnClickListener(getBackClickListener())

        return constraintLayout
    }

    private fun showBackCard(cardView: CardView) {
        isShowingBackCard = true
        cardView.elevation = 0f
        val cardViewBack = cardView.findViewById<RelativeLayout>(R.id.includeLayoutBack)
        val cardViewFront = cardView.findViewById<RelativeLayout>(R.id.includeLayout)
        cardView.cameraDistance = 12f * cardView.width

        val anim = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f).apply {
            duration = 250
        }

        anim.addListener(object: Animator.AnimatorListener {
            override fun onAnimationCancel(animation: Animator?) = Unit
            override fun onAnimationStart(animation: Animator?) = Unit
            override fun onAnimationRepeat(animation: Animator?) = Unit
            override fun onAnimationEnd(animation: Animator?) {
                cardViewFront.alpha = 0f
                cardViewBack.alpha = 1f
                cardViewBack.visibility = View.VISIBLE
                cardViewFront.visibility = View.GONE
            }
        })

        val anim1 = ObjectAnimator.ofFloat(cardView, "rotationY", -90f, 0f).apply {
            duration = 250
        }

        val set = AnimatorSet()
        set.playSequentially(anim, anim1)
        set.start()

    }

    private fun showFrontCard(cardView: CardView) {
        isShowingBackCard = false
        cardView.elevation = 0f
        val cardViewBack = cardView.findViewById<RelativeLayout>(R.id.includeLayoutBack)
        val cardViewFront = cardView.findViewById<RelativeLayout>(R.id.includeLayout)
        cardView.cameraDistance = 12f * cardView.width

        val anim = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f).apply {
            duration = 250
        }

        anim.addListener(object: Animator.AnimatorListener {
            override fun onAnimationCancel(animation: Animator?) = Unit
            override fun onAnimationStart(animation: Animator?) = Unit
            override fun onAnimationRepeat(animation: Animator?) = Unit
            override fun onAnimationEnd(animation: Animator?) {
                cardViewFront.alpha = 1f
                cardViewBack.alpha = 0f
                cardViewFront.visibility = View.VISIBLE
                cardViewBack.visibility = View.GONE
            }
        })

        val anim1 = ObjectAnimator.ofFloat(cardView, "rotationY", -90f, 0f).apply {
            duration = 250
        }

        val set = AnimatorSet()
        set.playSequentially(anim, anim1)
        set.start()
    }

    private fun isCreditCardValid() {
        Log.d(TAG, "isCreditCardValid: creditCardCvc.text = ${creditCardCvc.text.length}")
        if (creditCardEditExpDate.isDateValid && stripeCardNumberEditText.isCardNumberValid && creditCardCvc.text.length == 3) {
            enableButton()
        } else {
            disableButton()
        }
    }

    private fun enableButton() {
        saveButton.isEnabled = true
        saveButton.animate().alpha(1f)
    }

    private fun disableButton() {
        saveButton.isEnabled = true
        saveButton.animate().alpha(0.5f)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun changeBackgroundColor (brand: String) {
        Log.d(TAG, "changeBackgroundColor: called")
        val cardViewToAnimate = if (frontCardView.alpha == 1f) frontCardView else backCardView
        val cardViewToChangeBackground = if (frontCardView.alpha == 0f) frontCardView else backCardView

        val gradDrawable = (cardViewToAnimate.background as GradientDrawable)
        val tempGradientDrawable = GradientDrawable(GradientDrawable.Orientation.TR_BL, gradDrawable.colors)
        var startColorFromFront = tempGradientDrawable.colors[0]
        var endColorFromFront = tempGradientDrawable.colors[1]

        when(brand) {
            "Visa" -> {
                changeBackgroundTo(
                    R.drawable.background_visa,
                    cardViewToChangeBackground,
                    startColorFromFront,
                    tempGradientDrawable,
                    endColorFromFront,
                    cardViewToAnimate
                )
            }

            "MasterCard" -> {
                changeBackgroundTo(
                    R.drawable.background_mastercard,
                    cardViewToChangeBackground,
                    startColorFromFront,
                    tempGradientDrawable,
                    endColorFromFront,
                    cardViewToAnimate
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun changeBackgroundTo(
        drawableId: Int,
        cardViewToChangeBackground: RelativeLayout,
        startColorFromFront: Int,
        gradDrawable: GradientDrawable,
        endColorFromFront: Int,
        cardViewToAnimate: RelativeLayout
    ) {
        var startColorFromFront1 = startColorFromFront
        var endColorFromFront1 = endColorFromFront
        val gradientDrawable =
            mainActivity.resources.getDrawable(drawableId, null) as GradientDrawable
        cardViewToChangeBackground.background = gradientDrawable

        val startColorToFront = gradientDrawable.colors[0]
        val endColorToFront = gradientDrawable.colors[1]

        val startColorAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), startColorFromFront1, startColorToFront)
        startColorAnimation.duration = 250
        startColorAnimation.addUpdateListener { animator ->
            startColorFromFront1 = animator.animatedValue as Int
            gradDrawable.colors = intArrayOf(startColorFromFront1, endColorFromFront1)
            cardViewToAnimate.background = gradDrawable
        }
        startColorAnimation.start()

        val endColorAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), endColorFromFront1, endColorToFront)
        endColorAnimation.duration = 250
        endColorAnimation.addUpdateListener { animator ->
            endColorFromFront1 = animator.animatedValue as Int
            gradDrawable.colors = intArrayOf(startColorFromFront1, endColorFromFront1)
            cardViewToAnimate.background = gradDrawable
        }
        endColorAnimation.start()
    }

    private fun changeImageDrawable(brand: String) {
        if (brand == "Visa") {
            creditCardEditBrand.setImageDrawable(mainActivity.resources.getDrawable(R.drawable.ic_visa_initial_white, null))
        } else if ( brand == "MasterCard") {
            creditCardEditBrand.setImageDrawable(mainActivity.resources.getDrawable(R.drawable.ic_mastercard_initial_white, null))
        }
    }


    private fun getBackClickListener(): View.OnClickListener? {
        return View.OnClickListener {
            // add animation and show list of cards
            val paymentFragment = PaymentFragment(mainActivity)
            mainActivity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_placeholder, paymentFragment)
                .commitAllowingStateLoss()

//            val scene = Scene(rootLayout, getPaymentMethodView())
//            val fade = Fade().setDuration(300)
//            TransitionManager.go(scene, fade)
        }
    }

    private fun getOkClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val monthYear = creditCardEditExpDate.text.toString().split("/")
            val card = Card.Builder(stripeCardNumberEditText.text.toString(), monthYear[0].toInt(), monthYear[1].toInt(), creditCardCvc.text.toString()).name(creditCardEditHolderName.text.toString())
            val view: View? = mainActivity.window?.decorView?.rootView
            if (card == null) {
                Snackbar.make(view!!, "Invalid data", Snackbar.LENGTH_LONG).show()
            } else {
                //write card in shared preferences
                addTheNewCard(card.build())
                //writeUserToDb(user?.uid?.hashCode().toString(), 1124350)
                Snackbar.make(view!!, "Correct", Snackbar.LENGTH_LONG).show()

                val paymentFragment = PaymentFragment(mainActivity)
                mainActivity.supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_placeholder, paymentFragment)
                    .commitAllowingStateLoss()

//                val scene = Scene(rootLayout, getPaymentMethodView())
//                val fade = Fade().setDuration(300)
//                TransitionManager.go(scene, fade)
            }
        }
    }
}