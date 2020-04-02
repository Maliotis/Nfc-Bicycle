package com.example.nfc_.helpers

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.io.IOException
import java.nio.charset.Charset
import android.nfc.FormatException
import android.util.Log


class NFCUtil {

    companion object {

        fun createNFCMessage(payload: String, intent: Intent?, isWrite: Boolean) : Any {
            try {
                val pathPrefix = "example.com:nfc_"
                val nfcRecord = NdefRecord(
                    NdefRecord.TNF_MIME_MEDIA,
                    pathPrefix.toByteArray(Charset.forName("US-ASCII")),
                    ByteArray(0),
                    payload.toByteArray()
                )
                val nfcMessage = NdefMessage(arrayOf(nfcRecord))
                intent?.let {
                    val tag: Tag = it.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                    return if (isWrite) {
                        writeMessageToTag(nfcMessage, tag)
                    } else {
                        readFromTag(tag)
                    }
                }
                return false
            } catch (e: IOException) {
                Log.e(TAG, "createNFCMessage: e = ${e.localizedMessage}" )
                e.printStackTrace()
                return false
            }
        }

        private fun writeToNfc(ndef: Ndef?, message: String) {

            if (ndef != null) {

                try {
                    ndef.connect()
                    val mimeRecord =
                        NdefRecord.createMime("text/plain", message.toByteArray(Charset.forName("US-ASCII")))
                    ndef.writeNdefMessage(NdefMessage(mimeRecord))
                    ndef.close()
                    //Write Successful

                } catch (e: IOException) {
                    e.printStackTrace()

                } catch (e: FormatException) {
                    e.printStackTrace()
                } finally {

                }

            }
        }

        private fun readFromNFC(tag: Tag?): String {

            var message = ""
            try {
                val ndef = Ndef.get(tag)
                ndef.connect()
                val ndefMessage = ndef.ndefMessage
                message = String(ndefMessage.records[0].payload)
                Log.d("NFCUtil", "readFromNFC: $message")

                ndef.close()

            } catch (e: IOException) {
                e.printStackTrace()

            } catch (e: FormatException) {
                e.printStackTrace()
            }
            return message
        }

        private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {

            try {
                val nDefTag = Ndef.get(tag)

                nDefTag?.let {
                    it.connect()
                    if (it.maxSize < nfcMessage.toByteArray().size) {
                        //Message to large to write to NFC tag
                        return false
                    }
                    if (it.isWritable) {
                        it.writeNdefMessage(nfcMessage)
                        it.close()
                        //Message is written to tag
                        return true
                    } else {
                        //NFC tag is read-only
                        return false
                    }
                }

                val nDefFormatableTag = NdefFormatable.get(tag)

                nDefFormatableTag?.let {
                    try {
                        it.connect()
                        it.format(nfcMessage)
                        it.close()
                        //The data is written to the tag
                        return true
                    } catch (e: IOException) {
                        //Failed to format tag
                        return false
                    }
                }
                //NDEF is not supported
                return false

            } catch (e: Exception) {
                //Write operation has failed
            }
            return false
        }

        fun readFromTag(tag: Tag?): String {

            var message: String = ""
            val ndefTag = Ndef.get(tag)

            ndefTag?.let {
                it.connect()
                val ndefMessage = it.cachedNdefMessage
                if (ndefMessage != null)
                    message = String(ndefMessage.records[0].payload)
                Log.d("NFCUtil", "readFromNFC: $message")
                it.close()
            }

            return message
        }

        fun <T> enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType: Class<T>) {
            val pendingIntent = PendingIntent.getActivity(
                activity, 0,
                Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            )
            val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
            val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            val techDetected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
            val filters = arrayOf(techDetected, tagDetected, ndefDetected)

            val TechLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, TechLists)
        }

        fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
            nfcAdapter.disableForegroundDispatch(activity)
        }
    }
}