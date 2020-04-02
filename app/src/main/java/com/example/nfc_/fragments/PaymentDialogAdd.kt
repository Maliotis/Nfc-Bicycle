package com.example.nfc_.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context.MODE_PRIVATE
import android.widget.Button
import com.example.nfc_.R


/**
 * Created by petrosmaliotis on 2019-10-07.
 */

class PaymentDialogAdd(var ctx: Context, var activity: Activity) : DialogFragment() {

    private lateinit var builder: AlertDialog.Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val view = getPaymentMethodView()
        builder.apply {
            setTitle("Select Card")
            setView(view)
        }

        return builder.create()
    }

    private fun getPaymentMethodView(): View {
        val view: LinearLayout = activity.findViewById(R.id.list_card_view)
        val recyclerView: RecyclerView = activity.findViewById(R.id.recycler_view_card)
        val addButton: Button = activity.findViewById(R.id.add_button)
        addButton.setOnClickListener {
            onAddNewCardClick()
        }

        val dataSet: Array<String> = getDataSet()
        //val adapterManager: RecyclerViewAdapter = RecyclerViewAdapter()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(ctx)
            //adapter = adapterManager
        }

        return view
    }

    private fun getDataSet(): Array<String> {
        //TODO: get paymentMethods
        val editor = activity.getSharedPreferences("preferenceName", MODE_PRIVATE)
        return   editor.getStringSet("cards", mutableSetOf()).toTypedArray()
    }

    private fun onAddNewCardClick() {
        //change to on the enter payment details screen
        val paymentDialogCard = PaymentDialogCard(ctx, activity)
        //paymentDialogCard.show(activity.supportFragmentManager, "Payment")

        //then when users adds card use that code
        addTheNewCard()
    }

    private fun addTheNewCard() {
        val editor = activity.getSharedPreferences("preferenceName", MODE_PRIVATE)
        val dataSet = editor.getStringSet("cards", mutableSetOf())
        dataSet.add("the new card")
        val newEditor = editor.edit()
        newEditor.putStringSet("cards", dataSet)
        newEditor.apply()
    }
}