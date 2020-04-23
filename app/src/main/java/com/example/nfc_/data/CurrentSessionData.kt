package com.example.nfc_.data

import android.graphics.Bitmap

/**
 * Created by petrosmaliotis on 17/04/2020.
 */
data class CurrentSessionData(var image: Bitmap? = null,
                              var title: String? = null,
                              var value: String? = null) : DataDSL