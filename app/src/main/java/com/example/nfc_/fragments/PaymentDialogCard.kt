package com.example.nfc_.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import com.example.nfc_.helpers.DetailsLookup
import com.example.nfc_.R
import com.example.nfc_.adapters.RecyclerViewAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.stripe.android.model.*
import com.stripe.android.view.CardMultilineWidget

var tracker: SelectionTracker<Long>? = null

class PaymentDialogCard(context: Context, private var mainActivity: Activity) : DialogFragment() {

    val TAG = PaymentDialogCard::class.java.canonicalName
    var ctx = context
    var cardInputWidget: CardMultilineWidget? = null
    private lateinit var builder: AlertDialog.Builder

    private lateinit var rootLayout: ConstraintLayout

    var inflater: LayoutInflater? = null
    lateinit var recyclerView: RecyclerView
    lateinit var adapterManager: RecyclerViewAdapter

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        rootLayout = ConstraintLayout(context)
        rootLayout.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT)
        val view = getPaymentMethodView()
        //rootLayout.addView(view)
        builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("Select Card")
            //setView(view)
            setView(rootLayout)
        }

        adapterManager.notifyDataSetChanged()
        tracker = SelectionTracker.Builder<Long>(
            "mySelection",
            recyclerView,
            StableIdKeyProvider(recyclerView),
            DetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        adapterManager.tracker = tracker

        val scene = Scene(rootLayout, view)

        val fadeTransition: Transition = Fade()
        TransitionManager.go(scene, fadeTransition)
        recyclerView.isFocusable = true

        return builder.create()

        //return addCard()
    }


    private fun getPaymentMethodView(): View {
        inflater = LayoutInflater.from(activity!!)
        val view = inflater?.inflate(R.layout.list_cards, null)
        //val view: LinearLayout = mainActivity.findViewById(R.id.list_card_view)

        recyclerView = view?.findViewById(R.id.recycler_view_card)!!
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val addButton: Button = view.findViewById(R.id.add_button)
        addButton.setOnClickListener {
            onAddNewCardClick()
        }

        val dataSet: MutableList<Pair<String, String>> = getDataSetLastFourDigits()

        adapterManager = RecyclerViewAdapter(dataSet.toTypedArray(), ctx)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = adapterManager
        }

        return view!!
    }


    private fun getDataSetLastFourDigits(): MutableList<Pair<String, String>> {
        val editor = mainActivity.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
        val array = editor.getStringSet("cards", mutableSetOf()).toTypedArray()
        var returnList: MutableList<Pair<String, String>> = mutableListOf()
        array.forEach {
            val gson = Gson()
            val card: Card = gson.fromJson(it, Card::class.java)
            val pair = Pair(card.brand!!, card.last4!!)
            returnList.add(pair)

        }
        return returnList
    }

    private fun onAddNewCardClick() {
        val anotherScene = Scene(rootLayout, addCardView()!!)
        val fade: Transition = Fade().setDuration(300)
        val otherTransition: Transition = ChangeBounds()
        otherTransition.setPathMotion(ArcMotion())
        TransitionManager.go(anotherScene, otherTransition)
        //then when users adds card use that code
        //addTheNewCard()
    }

    private fun addTheNewCard() {
        val editor = mainActivity.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
        var dataSet = editor.getStringSet("cards", mutableSetOf())
        if (dataSet == null) dataSet = mutableSetOf() // defensive check
        //create json of card and store it as string
        val gson = Gson()
        val jsonString = gson.toJson(cardInputWidget?.card)
        dataSet.add(jsonString)
        val newEditor = editor.edit()
        newEditor.putStringSet("cards", dataSet)
        newEditor.apply()
    }

    private fun addCardView(): View? {
        rootLayout.removeAllViews()
        val constraintLayout = inflater?.inflate(R.layout.card_details, null)
        cardInputWidget = constraintLayout?.findViewById(R.id.cardMultilineWidget)
        cardInputWidget?.setCardNumber("4242-4242-4242-4242")
        cardInputWidget?.setCvcLabel("124")
        val okButton = constraintLayout?.findViewById<Button>(R.id.add_ok_button)
        val backButton = constraintLayout?.findViewById<Button>(R.id.back_card_button)

        val okClickListener = getOkClickListener()
        okButton?.setOnClickListener(okClickListener)
        //TODO add back button as well
        val backClickListener = getBackClickListener()
        backButton?.setOnClickListener(backClickListener)

        return constraintLayout
    }

    private fun getBackClickListener(): View.OnClickListener? {
        return View.OnClickListener {
            // add animation and show list of cards
            val scene = Scene(rootLayout, getPaymentMethodView())
            val fade = Fade().setDuration(300)
            TransitionManager.go(scene, fade)
        }
    }

    private fun getOkClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val card = cardInputWidget?.card
            val view: View? = mainActivity.window?.decorView?.rootView
            if (card == null) {
                Snackbar.make(view!!, "Invalid data", Snackbar.LENGTH_LONG).show()
            } else {
                //write card in shared preferences
                addTheNewCard()
                //writeUserToDb(user?.uid?.hashCode().toString(), 1124350)
                Snackbar.make(view!!, "Correct", Snackbar.LENGTH_LONG).show()

                //TODO add animation and show list of cards
            }
        }
    }

}