package com.example.nfc_.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import com.example.nfc_.helpers.Timer


class TimeCounterService : Service() {

    val TAG = TimeCounterService::class.java.simpleName

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    override fun onCreate() {
        super.onCreate()

        val thread = HandlerThread(
            "ServiceStartArguments",
            Process.THREAD_PRIORITY_BACKGROUND
        )
        thread.start()
        serviceLooper = thread.looper
        serviceHandler = ServiceHandler(serviceLooper!!, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = serviceHandler!!.obtainMessage() // returns a Message
        msg.arg1 = startId
        serviceHandler!!.sendMessage(msg)

        return START_STICKY // START_STICKY flag will restart
        // the process if it's killed byt the system
    }

    override fun onDestroy() {
        val elapsedTime = Timer.instance()?.getElapsedTimeSmart()
        Log.d(TAG, "onDestroy called: elapsed time = $elapsedTime")
        Timer.instance()?.stop()
    }

    override fun onBind(intent: Intent): IBinder {
        return Messenger(serviceHandler).binder
    }

    class ServiceHandler(looper: Looper, val context: Context) : Handler(looper) {
        val TAG = ServiceHandler::class.java.simpleName

        override fun handleMessage(msg: Message?) {
            Log.i(TAG, "Starting timer... ${msg?.arg1}")
            val timer = Timer.instance()
            timer?.start()
        }

    }


}
