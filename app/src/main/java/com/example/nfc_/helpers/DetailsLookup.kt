package com.example.nfc_.helpers

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.nfc_.adapters.RecyclerViewAdapter

/**
 * Created by petrosmaliotis on 2019-10-20.
 */

class DetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as RecyclerViewAdapter.ViewHolder)
                .getItemDetails()
        }
        return null
    }

}