package com.example.nfc_.helpers

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AlertDialog

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nfc_.activities.MainActivity


const val NFC_PERMISSION_REQUEST = 1
const val ACCESS_FINE_LOCATION_REQUEST = 2
const val REQUEST_ID_MULTIPLE_PERMISSIONS = 3


class Permission(var activity: MainActivity) {



    companion object {
        val TAG = Permission::class.java.simpleName
        var isNfcGranted = false
        var isLocationGranted = false
        var isActivityRecognitionGranted = false
        var activity: MainActivity? = null

        fun checkPermission(activity: MainActivity) {
            this.activity = activity
            var listOfPermissions: ArrayList<String> = arrayListOf()
            val nfcPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.NFC)
            val activityRecognitionPermission = ContextCompat.checkSelfPermission(activity, "com.google.android.gms.permission.ACTIVITY_RECOGNITION")
            val locationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            val granted = PackageManager.PERMISSION_GRANTED

            if (nfcPermission != granted) {
                listOfPermissions.add(Manifest.permission.NFC)
            } else {
                isNfcGranted = true
            }

            if (locationPermission != granted) {
                listOfPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                isLocationGranted = true
            }

            if (activityRecognitionPermission != granted) {
                listOfPermissions.add("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
            } else {
                isActivityRecognitionGranted = true
            }

            if (listOfPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(activity, listOfPermissions.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS )
            }

        }

        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            when (requestCode) {
                REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                    var perms = mutableMapOf<String, Int>()
                    if (grantResults.isNotEmpty()) {
                        for (index in permissions.indices) {
                            perms.put(permissions[index], grantResults[index])
                        }
                        if (perms.get(Manifest.permission.NFC) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get("com.google.android.gms.permission.ACTIVITY_RECOGNITION") == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, " ")
                            isNfcGranted = true
                            isLocationGranted = true
                            isActivityRecognitionGranted = true
                        } else {
                            Log.d(TAG, "Permissions were not  granted show rationale")
                            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.NFC)
                                || ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(activity!!, "com.google.android.gms.permission.ACTIVITY_RECOGNITION")
                            ) {
                                showDialogOK("NFC, Location Services and Activity Recognition Permission are required for this app",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkPermission(activity!!)
                                            DialogInterface.BUTTON_NEGATIVE -> {
                                            }
                                        }
                                    })
                            }
                        }

                    }
                    return
                }

                // Add other 'when' lines to check for other
                // permissions this app might request.
                else -> {
                    // Ignore all other requests.
                }
            }
        }

        private fun showDialogOK(
            message: String,
            okListener: DialogInterface.OnClickListener
        ) {
            AlertDialog.Builder(activity!!)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
        }
    }
}