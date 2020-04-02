package com.example.nfc_.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.edit
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.nfc_.R
import com.example.nfc_.fragments.PaymentDialogCard
import com.google.gson.Gson
import com.stripe.android.model.Card


var context:Context? = null
val TAG = RecyclerViewAdapter::class.java.simpleName
private var lastChecked: CheckBox? = null

class RecyclerViewAdapter(private val dataSet: Array<Pair<String, String>>,
                          private val ctx: Context): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
        context = ctx
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootLayout = LayoutInflater.from(parent.context).inflate(R.layout.adapter_item, parent,false)
        val numberTextView: TextView = rootLayout.findViewById(R.id.card_number_text)
        val brandTextView: TextView = rootLayout.findViewById(R.id.brand_text)
        val checkBox: CheckBox = rootLayout.findViewById(R.id.card_checkBox)

        return ViewHolder(
            rootLayout = rootLayout,
            numberTextView = numberTextView,
            brandTextView = brandTextView,
            checkBox = checkBox
        )
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int {
        return dataSet.size
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.brandTextView.text = dataSet[position].first
        holder.numberTextView.text = dataSet[position].second
        tracker?.let {
            holder.bind(it.isSelected(position.toLong()))
        }

        val row = getRowForCheckedCheckBox()
        if (row != -1 && row == position) {
            holder.checkBox.isChecked = true
            holder.checkBox.invalidate()
        }
    }

    private fun getRowForCheckedCheckBox(): Int? {
        val row = context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
            ?.getInt("card_to_use_row", -1)
        return row
    }

    // VIEW-HOLDER

    class ViewHolder(val rootLayout: View,
                     val numberTextView: TextView,
                     val brandTextView: TextView,
                     val checkBox: CheckBox) : RecyclerView.ViewHolder(rootLayout) {



        init {
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // Save the checked option
                    if (lastChecked == null) {
                        lastChecked = checkBox
                    } else {
                        lastChecked!!.isChecked = false
                        lastChecked = checkBox
                    }
                    saveCardForUse(layoutPosition)
                } else {
                    deleteCardToUse(layoutPosition)
                }

            }
        }

        private fun saveCardForUse(layoutPosition: Int) {
            Log.d(TAG, "saveCardForUse: caleld")
            val editor = context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
            val set = editor?.getStringSet("cards", mutableSetOf())?.toTypedArray()
            val string = set?.get(layoutPosition)
            val gson = Gson()
            val card: Card? = gson.fromJson(string, Card::class.java)
            card?.last4?.run {
                if(this == numberTextView.text) {
                    context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)?.edit {
                        putString("card_to_use", string)
                        putInt("card_to_use_row", layoutPosition)
                    }
                }
            }

        }

        private fun deleteCardToUse(layoutPosition: Int) {
            Log.d(TAG, "deleteCardToUse: called")
            context?.getSharedPreferences("preferenceName", Context.MODE_PRIVATE)?.edit {
                putString("card_to_use", "")
                putInt("card_to_use_row", -1)
            }

        }

        fun bind(isActivated: Boolean = false) {
            //itemView.isActivated = isActivated
            var activated = isActivated
            if (checkBox.isChecked && isActivated) activated = false
            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = activated
            checkBox.invalidate()
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
                override fun inSelectionHotspot(e: MotionEvent): Boolean = true
            }

    }

}
