package com.example.nfc_.helpers

/**
 * Created by petrosmaliotis on 2019-11-13.
 */

class Timer {
    private var startTime: Long = 0
    private var stopTime: Long = 0
    private var running = false

    companion object {
        private var timer: Timer? = null
        fun instance(): Timer? {
            if (timer == null) {
                timer = Timer()
            }
            return timer
        }
    }

    private constructor()

    fun start() {
        startTime = System.currentTimeMillis()
        running = true
    }


    fun stop() {
        stopTime = System.currentTimeMillis()
        running = false
    }


    // elaspsed time in milliseconds
    fun getElapsedTime(): Long {
        return if (running) {
            System.currentTimeMillis() - startTime
        } else stopTime - startTime
    }


    //elaspsed time in seconds
    fun getElapsedTimeSecs(): Long {
        var elapsed: Long = 0
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime) / 1000 % 60
        }
        return elapsed
    }

    //elaspsed time in minutes
    fun getElapsedTimeMin(): Long {
        var elapsed: Long = 0
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime) / 1000 / 60 % 60
        }
        return elapsed
    }

    //elaspsed time in hours
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
        if (hours > 0) timeString = timeString + "Hours: " + hours + ", "
        if (minutes > 0) timeString = timeString + "Minutes: " + minutes + ", "
        timeString = timeString + "Seconds: " + seconds

        return timeString
    }
}