package com.example.nfc_.services

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log


/**
 * Created by petrosmaliotis on 11/03/2020.
 */

val TAG = ActivityRecognitionService::class.java.canonicalName
class ActivityRecognitionService : IntentService(TAG) {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    override fun onCreate() {
        super.onCreate()

        val thread = HandlerThread(
            "ActivityRecognitionStartArguments",
            Process.THREAD_PRIORITY_BACKGROUND
        )
        thread.start()
        serviceLooper = thread.looper
        serviceHandler = ServiceHandler(serviceLooper!!, this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = serviceHandler!!.obtainMessage()
        msg.arg1 = startId
        serviceHandler!!.sendMessage(msg)

        return Service.START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
    }

    override fun onHandleIntent(intent: Intent?) {

    }

    class ServiceHandler(looper: Looper, val context: Context) : Handler(looper) {
        val TAG = ServiceHandler::class.java.simpleName

        override fun handleMessage(msg: Message?) {
            Log.d(TAG, "handleMessage: message received")
        }
    }

}