package com.example.nfc_.helpers

/**
 * Created by petrosmaliotis on 16/03/2020.
 */

fun <T> ifElse(condition: Boolean, primaryResult: T, secondaryResult: T): T {
    return if (condition) {
        primaryResult
    } else {
        secondaryResult
    }
}