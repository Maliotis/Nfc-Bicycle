package com.example.nfc_.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.nfc_.R
import com.example.nfc_.activities.MainActivity
import com.example.nfc_.helpers.rentButton

/**
 * Created by petrosmaliotis on 19/03/2020.
 */
var rentButton: Button? = null
class MainFragment(private val mainActivity: MainActivity) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.content_main, container, false)
        rentButton = view.findViewById(R.id.rent_button)
        rentButton!!.setOnClickListener(onClickRentButton(rentButton!!))
        return view
    }

    private fun onClickRentButton(view: View): View.OnClickListener {
        return View.OnClickListener {
            rentButton(arrayListOf<View>(view), context = mainActivity)
        }
    }
}