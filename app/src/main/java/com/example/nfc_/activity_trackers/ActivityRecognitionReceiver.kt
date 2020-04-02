package com.example.nfc_.activity_trackers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import com.example.nfc_.database.entities.RentTransaction
import com.example.nfc_.helpers.DatabaseClient
import com.example.nfc_.services.LocationService
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

/**
 * Created by petrosmaliotis on 2019-10-01.
 */
class ActivityRecognitionReceiver: BroadcastReceiver() {

    private val TAG = ActivityRecognitionReceiver::class.java.canonicalName

    override fun onReceive(context: Context?, intent: Intent?) {

        RunAyncTask().execute(Pair(context!!, intent!!))
        Log.d(TAG, "onReceive: called")
    }

    companion object {
        class RunAyncTask : AsyncTask<Pair<Context, Intent>, Void, Void>() {

            val TAG = RunAyncTask::class.java.simpleName

            override fun doInBackground(vararg params: Pair<Context, Intent>?): Void? {
                //unwrap parameters
                val intent = params[0]?.second
                val context = params[0]?.first
                // get db reference
                DatabaseClient.applicationContext = context
                val userDao = DatabaseClient.db?.userDao()
                if (ActivityTransitionResult.hasResult(intent)) {
                    val result = ActivityTransitionResult.extractResult(intent)!!
                    for (event in result.transitionEvents) {
                        // chronological sequence of events....
                        Log.i(TAG, "event: ${event.activityType}")
                        if (event.activityType == DetectedActivity.ON_BICYCLE) {

                            val userWRT = userDao?.getUserWithActiveRentTransaction(true,true) // get the active transaction
                            var rentTransition: RentTransaction? = null
                            userWRT?.rentTransactions?.forEach {
                                if (it.active == true) {
                                    rentTransition = it
                                    return@forEach
                                }
                            }
                            val isActive = rentTransition?.active ?: false
                            if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER && isActive) {
                                startCounting(context)
                            } else {
                                stopCounting(context)
                            }
                        } else { // Anything else
                            if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                stopCounting(context)
                            }
                        }
                    }
                }
                return null
            }
            private fun startCounting(context: Context?) {
                // register a receiver to get the users location
                val intentService = Intent(context, LocationService::class.java)
                context?.startService(intentService)
            }
            private fun stopCounting(context: Context?) {
                val intentService = Intent(context, LocationService::class.java)
                context?.stopService(intentService)
            }

        }
    }
}



// FUNCTIONS

/**
 * This will be triggered when the user wants to see how many miles he done in that trip.
 */
public fun showMiles() {

}