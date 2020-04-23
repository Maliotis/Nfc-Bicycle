package com.example.nfc_.data

import java.lang.RuntimeException
import kotlin.reflect.KClass

/**
 * Created by petrosmaliotis on 17/04/2020.
 */

inline fun <reified T: DataDSL> constructObject(block: T.() -> Unit): T {
    val kClass: KClass<T> = T::class
    var p: T? = null
    kClass.constructors.forEach { con ->
        if (con.parameters.isEmpty()) {
            p = con.call()
        }
    }
    if (p == null) {
        throw RuntimeException("Class could not be instantiated")
    }
    p!!.block()
    return p!!
}