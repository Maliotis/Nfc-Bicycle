package com.example.nfc_.fragments

import android.animation.Animator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.nfc_.R
import org.jetbrains.anko.find

/**
 * Created by petrosmaliotis on 17/03/2020.
 */
private val TAG = AlertChooseBankDetails::class.java.simpleName
class AlertChooseBankDetails(val ctx: Context): DialogFragment() {

    private lateinit var builder: AlertDialog.Builder
    private var inflater: LayoutInflater? = null
    private lateinit var button: Button
    private lateinit var lottieAnimationView: LottieAnimationView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(ctx)
        val view = getRootView()
        builder.apply {
            setTitle("Oops!")
            setView(view)
        }

        return builder.create()
    }

    private fun getRootView(): View {
        inflater = LayoutInflater.from(activity!!)
        val view = inflater?.inflate(R.layout.dialog_fragment_alert_choose_bank_details, null)
        lottieAnimationView = view!!.findViewById(R.id.lottie_credit_card)
        lottieAnimationView.speed = 0.5f
        lottieAnimationView.repeatMode = LottieDrawable.REVERSE
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        lottieAnimationView.playAnimation()
        lottieAnimationView.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                lottieAnimationView.setMinProgress(0.44f)
            }
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}

        })
        button = view!!.findViewById(R.id.ok_bank_details)
        button.setOnClickListener(okButtonOnClickListener())
        return view!!
    }

    private fun okButtonOnClickListener(): View.OnClickListener {
        Log.d(TAG, "okButtonOnClickListener: called")
        return View.OnClickListener {
            // TODO: Show the Dialog for bank details
        }
    }
 }