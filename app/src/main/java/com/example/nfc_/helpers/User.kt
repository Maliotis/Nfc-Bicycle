package com.example.nfc_.helpers

import com.google.firebase.database.IgnoreExtraProperties
import com.stripe.android.model.Token

/**
 * Created by Your name on 2019-07-08.
 */

@IgnoreExtraProperties
data class User(
    var username: String? = "",
    var email: String? = "",
    var token: Token? = null,
    var amount: Double? = null,
    var currency: String? = ""
)
