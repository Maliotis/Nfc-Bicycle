package com.example.nfc_.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.nfc_.BIKE_ANIM_IMG
import com.example.nfc_.R
import com.example.nfc_.data.DataDSL
import com.example.nfc_.data.HistoryTransactionData
import com.example.nfc_.data.constructObject
import org.jetbrains.anko.find

/**
 * Created by petrosmaliotis on 19/04/2020.
 */
class HistoryRecyclerViewAdapter(var data: MutableList<HistoryTransactionData>) : RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder>() {

    // VIEW HOLDER
    data class HistoryViewHolder(
        val rootLayout: RelativeLayout,
        val paymentImageView: ImageView,
        val lastFourTextView: TextView,
        val dateTextView: TextView,
        val amountTextView: TextView
    ) : RecyclerView.ViewHolder(rootLayout)

    override fun getItemCount(): Int {
        return data.size

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {

        val rootLayout: RelativeLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false) as RelativeLayout
        val paymentImageView: ImageView = rootLayout.findViewById(R.id.paymentImageView)
        val lastFourTextView: TextView = rootLayout.findViewById(R.id.lastFourTextView)
        val dateTextView: TextView = rootLayout.findViewById(R.id.dateTextView)
        val amountTextView: TextView = rootLayout.findViewById(R.id.amountTextView)
        return HistoryViewHolder(
            rootLayout,
            paymentImageView,
            lastFourTextView,
            dateTextView,
            amountTextView
        )

    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder as HistoryViewHolder
        holder.paymentImageView.setImageDrawable(data[position].imageDrawable)
        holder.lastFourTextView.text = data[position].lastFour
        holder.dateTextView.text = data[position].date
        holder.amountTextView.text = data[position].amount
    }
}