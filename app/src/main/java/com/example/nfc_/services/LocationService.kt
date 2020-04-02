package com.example.nfc_.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import com.example.nfc_.helpers.MyLocation
import com.example.nfc_.helpers.MyLocation.LocationResult


/**
 * Location service gets the users location every 1-3 sec
 * and calculates the distance between these intervals.
 * The data will be stored in a database.
 * Created by petrosmaliotis on 2020-01-02.
 */

class LocationService : Service() {

    val TAG = LocationService::class.java.simpleName

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate called")
        super.onCreate()
        val thread = HandlerThread("LocationServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        serviceLooper = thread.looper
        serviceHandler = ServiceHandler(serviceLooper!!, this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        val msg = serviceHandler!!.obtainMessage()
        msg.arg1 = startId
        serviceHandler!!.sendMessage(msg)

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private class ServiceHandler(looper: Looper,val context: Context) : Handler(looper) {

        val TAG = ServiceHandler::class.java.simpleName
        // Location var
        var latitude: Double? = 0.0
        var longitude: Double? = 0.0

        // Location Helpers
        var myLocation: MyLocation? = null // Make sure that we get the location every 1-3 sec
        var locationResult: LocationResult = object: LocationResult() {
            override fun gotLocation(location: Location?) {
                latitude = location?.latitude
                longitude = location?.longitude
                Log.d(TAG, "Location $location \n latitude = $latitude \n longitude = $longitude")
                //TODO calculate distance with the previous location and store the result

                // Send message to repeat the process
                sendMessage(Message())
            }

        }

        override fun handleMessage(msg: Message?) {
            myLocation = MyLocation()
            myLocation!!.init(context, locationResult)
        }
    }

}