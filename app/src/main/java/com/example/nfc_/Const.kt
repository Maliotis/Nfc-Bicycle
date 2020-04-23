package com.example.nfc_

const val NFC_ANIMATE_IMG = "nfc_animated_img.json"

const val BIKE_ANIM_IMG = "lottie_bicycle.json"

const val ACTION_PROCESS_ACTIVITY_TRANSITIONS = "com.example.nfc_.ACTION_PROCESS_ACTIVITY_TRANSITIONS"

fun buildPathClientSecret(user: String): String {
    return "/users/${user}/client_secret"
}

fun buildPathEvents(): String {
    return "/events"
}