package com.example.nfc_.helpers

import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Created by petrosmaliotis on 2019-11-13.
 */

class Timer {
    var startTime: Long = 0
        private set(value) { field = value}
    private var stopTime: Long = 0
    var running = false
    private var observable: Observable<Double>? = null


    companion object {
        private var timer: Timer? = null
        fun instance(): Timer? {
            if (timer == null) {
                timer = Timer()
            }
            return timer
        }
    }

    fun start() {
        startTime = System.currentTimeMillis()
        running = true
    }


    fun stop() {
        stopTime = System.currentTimeMillis()
        running = false
    }

    // elapsed time in milliseconds
    fun getElapsedTime(): Long {
        return if (running) {
            System.currentTimeMillis() - startTime
        } else stopTime - startTime
    }


    //elapsed time in seconds
    fun getElapsedTimeSecs(): Long {
        var elapsed: Long = 0
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime) / 1000 % 60
        }
        return elapsed
    }

    //elapsed time in minutes
    fun getElapsedTimeMin(): Long {
        var elapsed: Long = 0
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime) / 1000 / 60 % 60
        }
        return elapsed
    }

    //elapsed time in hours
    fun getElapsedTimeHour(): Long {
        var elapsed: Long = 0
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime) / 1000 / 60 / 60
        }
        return elapsed
    }

    fun getElapsedTimeSmart(): String {
        var timeString = ""
        val hours = getElapsedTimeHour()
        val minutes = getElapsedTimeMin()
        val seconds = getElapsedTimeSecs()
        if (hours > 0) timeString = timeString + "Hrs: " + hours + ", "
        if (minutes > 0) timeString = timeString + "Min: " + minutes + ", "
        timeString = timeString + "Sec: " + seconds

        return timeString
    }

    fun getTimeObservable(initialDelay: Long, period: Long, timeUnit: TimeUnit): Observable<Long> {
        return Observable.interval(initialDelay, period, timeUnit)
//            .flatMap {
//                Observable.create<String> { emitter ->
//                    Log.d(TAG, "getObservable: $it")
//                    emitter.onNext(getElapsedTimeSmart())
//                    emitter.onComplete()
//
//                }
//            }
    }
}