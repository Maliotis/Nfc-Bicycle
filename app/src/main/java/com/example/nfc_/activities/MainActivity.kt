package com.example.nfc_.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.marginBottom
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import com.example.nfc_.ACTION_PROCESS_ACTIVITY_TRANSITIONS
import com.example.nfc_.BuildConfig
import com.example.nfc_.R
import com.example.nfc_.activity_trackers.ActivityRecognitionReceiver
import com.example.nfc_.activity_trackers.initActivityRecognition
import com.example.nfc_.database.AppDatabase
import com.example.nfc_.database.entities.RentTransaction
import com.example.nfc_.database.entities.User
import com.example.nfc_.fragments.AlertChooseBankDetails
import com.example.nfc_.fragments.AuthConfirmDialog
import com.example.nfc_.fragments.MainFragment
import com.example.nfc_.fragments.PaymentDialogCard
import com.example.nfc_.helpers.*
import com.example.nfc_.fragments.rentButton
import com.example.nfc_.helpers.Timer
import com.example.nfc_.services.ActivityRecognitionService
import com.example.nfc_.services.TimeCounterService
import com.facebook.FacebookSdk
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.internal.safeparcel.SafeParcelableSerializer
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.stripe.android.*
import com.stripe.android.model.*
import com.stripe.android.stripe3ds2.init.ui.StripeUiCustomization
import org.jetbrains.anko.doAsync
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

var nfcAdapter: NfcAdapter? = null
var user: FirebaseUser? = null
val database = FirebaseDatabase.getInstance().reference
var secretKeyRef: DatabaseReference = database.child("/users/${user?.uid?.hashCode().toString()}/client_secret")
var listener: ValueEventListener? = null
val stripeKey = BuildConfig.stripe_key
lateinit var stripe: Stripe

const val ACTION_ACTIVITY_RECOGNITION = "com.example.nfc_.activities.Action"
private val TAG = MainActivity::class.java.canonicalName

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {


    private lateinit var mainLayout: ConstraintLayout
    private var isShowingLoginScreen: Boolean = false
    var rentSuccessfully: Boolean = false
    private var firstTime: Boolean = false
    private lateinit var auth: FirebaseAuth
    private lateinit var authConfirmDialog: AuthConfirmDialog
    private lateinit var db : AppDatabase
    private val RC_SIGN_IN: Int = 123
    private val RC_SIGN_UP: Int = 56

    private var apiClient: GoogleApiClient? = null

    // ANDROID - LIFECYCLE

    override fun onStart() {
        super.onStart()
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database.db"
        )
            .enableMultiInstanceInvalidation()
            .build()
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PaymentConfiguration.init(stripeKey)
        stripe = Stripe(
            this,
            PaymentConfiguration.getInstance().publishableKey
        )
        mainLayout = findViewById(R.id.fragment_placeholder)
        val mainFragment = MainFragment(this)
        supportFragmentManager.beginTransaction().add(R.id.fragment_placeholder, mainFragment).commit()
        initFirebaseDatabaseListener()

        checkIfUserIsLoggedIn()

        Permission.checkPermission(this)

        //rentButton = this.findViewById(R.id.rent_button)
        //rentButton?.setOnClickListener(this::onClick)



        val toolbar: Toolbar = this.findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        //If the nfc is not available show user a message explaining why they cant use the app
        if (nfcAdapter == null) {
            Toast.makeText(this, "Nfc is not available", Toast.LENGTH_LONG).show()
            // TODO: show dialog with sad face
            //finish()
            //return
        }

        updateUserCredentialUI()

        // Add the ActivityRecognitionReceiver and track users miles and usage

        val br = ActivityRecognitionReceiver()
        val filter = IntentFilter(ACTION_PROCESS_ACTIVITY_TRANSITIONS)
        registerReceiver(br, filter)

        //pending intent for ActivityRecognition
        val intent = Intent(this, ActivityRecognitionService::class.java)
        intent.action = ACTION_PROCESS_ACTIVITY_TRANSITIONS
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        //register ActivityRecognition listener
        val request: ActivityTransitionRequest =
            initActivityRecognition()
        val task = ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, pendingIntent)
        task.addOnSuccessListener {
                Log.i(TAG, "Task - ActivityRecognition - registered successfully")
            }
            .addOnFailureListener {
            Log.i(TAG, "Task - ActivityRecognition - registered un-successfully")
            }
            .addOnCompleteListener {
                Log.i(TAG, "Task - ActivityRecognition - completed")
                //emulateActivityTransitionToBicycle()
            }


    }

    // TEST ACTIVITY TRANSITION

    private fun emulateActivityTransitionToBicycle() {
        val p1 = Intent()
        p1.action = ACTION_PROCESS_ACTIVITY_TRANSITIONS
        val events: ArrayList<ActivityTransitionEvent> = ArrayList()
        var transitionEvent: ActivityTransitionEvent?
        transitionEvent = ActivityTransitionEvent(
            DetectedActivity.STILL,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT, SystemClock.elapsedRealtimeNanos()
        )
        events.add(transitionEvent)
        transitionEvent = ActivityTransitionEvent(
            DetectedActivity.ON_BICYCLE,
            ActivityTransition.ACTIVITY_TRANSITION_ENTER, SystemClock.elapsedRealtimeNanos()
        )
        events.add(transitionEvent)
        val result = ActivityTransitionResult(events)
        SafeParcelableSerializer.serializeToIntentExtra(
            result, p1,
            "com.google.android.location.internal.EXTRA_ACTIVITY_TRANSITION_RESULT"
        )
        sendBroadcast(p1)
    }

    // FIREBASE DATABASE LISTENERS

    private fun initFirebaseDatabaseListener() {
        database.child("/events").addValueEventListener(object: ValueEventListener
        /*database.addValueEventListener(object : ValueEventListener*/ {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (firstTime) {
                    val value = dataSnapshot.value
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        val last:HashMap<String, Any> = dataSnapshot.children.last().value as HashMap<String, Any>
                        if (!last.get("type")!!.equals("payment_intent.succeeded")) {
                            //show error and return
                            if (::authConfirmDialog.isInitialized) {
                                authConfirmDialog.dismiss()
                            }
                            val snackbar = Snackbar.make(mainLayout, "The payment was unsuccessful", Snackbar.LENGTH_LONG)//.show()
                            snackbar.addCallback(snackbarCallback())
                            snackbar.show()

                            return
                        }
                        val data = (last["data"] as HashMap<*, *>)

                        val charges = (data.get("object") as HashMap<*, *>)
                            .get("charges") as HashMap<*, *>

                        val map0 = (charges.get("data") as ArrayList<*>)[0] as HashMap<*, *>
                        val receiptUrl = map0.get("receipt_url") as String
                        // TODO: instead of showing the receipt store it in db
                        if (::authConfirmDialog.isInitialized) {
                            val webView = authConfirmDialog.relativeLayout.getChildAt(0) as WebView
                            webView.loadUrl(receiptUrl)
                        } else {
                            authConfirmDialog = AuthConfirmDialog(
                                context = this@MainActivity,
                                url = receiptUrl
                            )
                            authConfirmDialog.show(supportFragmentManager, "Confirm")
                        }
                        //writeContentToString(receiptUrl)
                    }
                }; firstTime = true
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun initFirebaseDatabaseListenerForSecretKeyRef() {
        if (listener != null) secretKeyRef.removeEventListener(listener!!)

        listener = secretKeyRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                // A new comment has been added, add it to the displayed list
                val clientSecretKey = dataSnapshot.children.last().value as String
                if (clientSecretKey?.isNotEmpty()!!) {
                    val pref = getSharedPreferences("preferenceName", Context.MODE_PRIVATE)
                    val dataSet = pref.getString("card_to_use", null)
                    if (dataSet != null && dataSet.isNotEmpty()) {
                        val card = Gson().fromJson(dataSet, Card::class.java)
                        stripe(card = card!!, secretKey = clientSecretKey)
                    }
                }
            }
        })

    }

    private fun createBackIcon(): ImageButton {
        val imageButton = ImageButton(this)
        imageButton.background = resources.getDrawable(R.drawable.ic_round_arrow_back_24px)
        val p = ViewGroup.LayoutParams(50, 50)
        imageButton.layoutParams = p
        return imageButton
    }

    // LOGIN

    private fun checkIfUserIsLoggedIn() {

        auth = FirebaseAuth.getInstance()
        FacebookSdk.sdkInitialize(this)
        //user = auth.currentUser

        if (user != null) {
            //TODO update ui
        } else {
            //show log in screen
            showLoginScreen()
        }
    }

    private fun showLoginScreen() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build())

// Create and launch sign-in intent
        isShowingLoginScreen = true
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.AppTheme)
                .setLogo(R.drawable.nfc_bicycle1)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            RC_SIGN_IN)



    }

    private fun updateUserCredentialUI() {
        if (user != null) {
            val profileImage: ImageView? = findViewById(R.id.profile_image)
            val profileName: TextView? = findViewById(R.id.profile_name)
            val profileEmail: TextView? = findViewById(R.id.profile_email)
            val navHeader: LinearLayout? = findViewById(R.id.nav_header)

            var url = user?.photoUrl
            val name = user?.displayName
            val email = user?.email
            val strUrl = url.toString().replace("s96-c", "s110-c")
            url = strUrl.toUri()
            Picasso.get().load(url).transform(CircleTransform()).into(profileImage)

            profileName?.text = name
            profileEmail?.text = email

        }
    }

    // ACTIVITY - PERMISSION RESULTS

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //PaymentMethodsActivityStarter request code
        if (requestCode == 40 && data != null) {

            Log.i("PaymentMethodsActivity", data.toString());
            //PaymentMethodsActivityStarter.Args
            //val result = PaymentMethodsActivityStarter.Result.fromIntent(data)
            //val paymentMethod = result?.paymentMethod
        }

        stripe.onPaymentResult(
            requestCode, data, PaymentConfiguration.getInstance().publishableKey,
            object : ApiResultCallback<PaymentIntentResult> {
                override fun onSuccess(result: PaymentIntentResult) {
                    // If authentication succeeded, the PaymentIntent will have
                    // user actions resolved; otherwise, handle the PaymentIntent
                    // status as appropriate (e.g. the customer may need to choose
                    // a new payment method)
                    val paymentIntent = result.intent
                    val status = paymentIntent.status
                    if (status == StripeIntent.Status.Succeeded) {
                        // TODO: show success UI
                        Log.i("SuccessUI", "Stripe returned succesful")
                        val snackbar = Snackbar.make(mainLayout, "Stripe Payment Was Successful", Snackbar.LENGTH_LONG)
                        snackbar.addCallback(snackbarCallback())
                        snackbar.show()
                    }
//                    else if (PaymentIntent.Status.RequiresPaymentMethod == status) {
//                        // attempt authentication again or
//                        // ask for a new Payment Method
//                    }
                }

                override fun onError(e: Exception) {
                    // handle error
                }
            })


        Log.i("activityResult", requestCode.toString())
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().currentUser
                secretKeyRef = database.child("/users/${user?.uid?.hashCode().toString()}/client_secret")
                initFirebaseDatabaseListenerForSecretKeyRef()
                if (!user?.isEmailVerified!!) {
                    user?.sendEmailVerification()?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "Email Sent")
                        }
                    }
                } else {
                    writeLoggedInUserToLocalDB(user)
                }
                updateUserCredentialUI()
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                val code = response?.error?.errorCode
                Log.e(TAG, "$code")
                Snackbar.make(window.decorView.rootView, "Sign in failed", Snackbar.LENGTH_LONG).show()
                showLoginScreen()
                return
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Permission.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.data != null && intent.data!!.query != null) {
            val host = intent.data!!.host
            val clientSecret = intent.data!!.getQueryParameter(
                "payment_intent_client_secret")

//            val retrievePaymentIntentParams = PaymentIntentParams.createRetrievePaymentIntentParams(
//                clientSecret!!)

            val stripe = Stripe(
                this,
                PaymentConfiguration.getInstance().publishableKey
            )

            // If you had a dialog open when your user went elsewhere,
            // remember to close it here.
            //redirectDialogController.dismissDialog();
        } else {
            val valid = isSavedCardToUseValid()
            if (!valid) {
                val alertChooseBankDetails = AlertChooseBankDetails(this)
                alertChooseBankDetails.show(supportFragmentManager, "AlertBankDetails")
            } else {
                setIntent(intent)
                writeTagOperation(intent)
            }
        }
    }

    // LOCAL DB TRANSACTIONS

    private fun writeLoggedInUserToLocalDB(user: FirebaseUser?) {
        doAsync {
            val email = user?.email
            val name = user?.displayName
            val userDb = User(uid = 0, firstName = name, lastName = null, email = email, logIn = true)
            db.userDao().setLoggedInUser(userDb)
        }
    }

    // ON CLICK CALLBACKS

    override fun onClick(v: View?) {
        val a = arrayListOf<View>()
        v?.let { a.add(it) }
        when (v?.id) {
            R.id.rent_button -> rentButton(a, this)
        }
    }

    // SNACKBAR CALLBACK

    fun snackbarCallback(): Snackbar.Callback {
        return object: Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                // If rent button is shown lift it up
                Log.d(TAG, "onShown: called")
                if (cardView != null) {
                    sb?.view?.height?.run {
                        //val diff = window.decorView.rootView.height - this
                        val translationY = cardView!!.marginBottom - 1.3 * this
                        cardView!!.animate().translationY(translationY.toFloat()).start()
                    }

                }
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                // If rent button is shown reset y value
                Log.d(TAG, "onDismissed: called")
                if (cardView != null) {
                    transientBottomBar?.view?.height?.run {
                        //val diff = window.decorView.rootView.height - this
                        val translationY = cardView!!.y + this
                        cardView!!.animate().translationY(0f).start()
                    }

                }
            }
        }
    }

    override fun onBackPressed() {
        if (isShowingLoginScreen == true) {
            return
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // DRAWER
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_history -> {
                // Handle the camera action
            }
            R.id.nav_payment -> {
                //TODO: show alert dialog for payment
                //show alert to add payment method
                //PaymentDialogAdd()
                val paymentDialogCard = PaymentDialogCard(this, this)
                paymentDialogCard.show(supportFragmentManager, "Payment")
            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_logout -> {
                auth.signOut()
                showLoginScreen()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    //NFC AND BIKE OPERATIONS

    private fun writeTagOperation(intent: Intent?) {
        /**
         * Logic to write on tag:
         * Create a hash value based on users credentials
         * write that value on the Tag
         * that value is also stored in firebase and locally in the device
         * once the the same Tag is read again the Tag resets
         *
         * NFC:
         * Nfc is a set telecommunication standards
         * Ndef is the format of data stored in the Tag
         * Ndef Message consists of Ndef records:
         * -Type Length
         * -Payload Length
         * -ID Length
         * -Record Type
         * -ID
         * -Payload
         *
         * TNF describes the record Type
         * TNF_EMPTY         | Indicates the record is empty.                                                    | 0x00       |
         * TNF_WELL_KNOWN    | Indicates the type field contains a well-known RTD type name.                     | 0x01       |
         * TNF_MIME_MEDIA    | Indicates the type field contains a media-type                                    | 0x02       |
         * TNF_ABSOLUTE_URI  | Indicates the type field contains an absolute-URI                                 | 0x03       |
         * TNF_EXTERNAL_TYPE | Indicates the type field contains an external type name                           | 0x04       |
         * TNF_UNKNOWN       | Indicates the payload type is unknown                                             | 0x05       |
         * TNF_UNCHANGED     | Indicates the payload is an intermediate or final chunk of a chunked NDEF Record.
         */

        var hashCode = 0
        if (user !=null) {
            hashCode = user?.uid.hashCode()
        }

        // Read from NFC Tag
        val valueNfcTag = NFCUtil.createNFCMessage(
            payload = hashCode.toString(),
            intent = intent,
            isWrite = false
        )

        //if the tag is empty write on it
        //TODO when the user writes on the tag automatically the bike unlocks and the timer starts counting
        if (valueNfcTag == "") {
            // User rents the bike write users hashcodeId to tag
            val messageWrittenSuccessfully = unlockBike(hashCode, intent)
            if (messageWrittenSuccessfully) addNewRentTransaction()

        } else { // tag exists
            val messageWrittenSuccessfully = lockBike(intent)
            // TODO Make the transaction
            // The amount will be based on the hourly usage of the bike
            val amount = calculateAmount(pricePerHour = 50.0)
            if (messageWrittenSuccessfully) addUserTransactionToFirebaseDB(user?.uid?.hashCode().toString(), amount)
            Log.i("Tag", "$messageWrittenSuccessfully")

        }


        //Toast.makeText(this, text, Toast.LENGTH_LONG).show()

        //TODO: once the text ha being written on to TAG animate button to close
        Log.i("hashCode", hashCode.toString())
    }

    private fun calculateAmount(pricePerHour: Double): Int {
        val elapsedTime: Long = Timer.instance()?.getElapsedTime() ?: return 150
        // 0.5£ per hour is 50/60/60/1000 = 0.00001388888 per milliSecond
        val pricePerMilliSecond = pricePerHour / 60L / 60L / 1000L
        // The amount should be at least 1.5£
        val amount = elapsedTime.times(pricePerMilliSecond)
        Log.d(TAG, "calculateAmount: amount = $amount")
        return if (amount < 150) 150
        else amount.toInt()
    }

    private fun addNewRentTransaction() {
        doAsync {
            val currentTime = Calendar.getInstance().time
            val user = db.userDao().getUserByEmail(email = user?.email)
            val userId = user?.uid ?: throw RuntimeException("user doesn't exist therefore there should be no transaction")
            val rentTransaction = RentTransaction(tid = 0, active = true, amount = null, date = currentTime.toString(), userCreatorId = userId)
            db.rentTransaction().insertAll(rentTransaction)
        }
    }

    private fun lockBike(intent: Intent?): Boolean {
        val messageWrittenSuccessfully =
            NFCUtil.createNFCMessage("", intent, isWrite = true)
        val text = ifElse(
            messageWrittenSuccessfully as Boolean,
            "Successful emptied the Tag",
            "Something When wrong emptying Try Again"
        )
        val view = findViewById<View>(R.id.main_content)
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
        snackbar.addCallback(snackbarCallback())
        snackbar.show()
        if (messageWrittenSuccessfully) {
            rentSuccessfully = false
            val timeServiceIntent = Intent(this, TimeCounterService::class.java)
            Log.d(TAG, "lockBike: Timer = ${Timer.instance()?.getElapsedTimeSmart()}")
            stopService(timeServiceIntent)
        }
        cancelButton?.performClick()
        return messageWrittenSuccessfully
    }

    private fun unlockBike(hashCode: Int, intent: Intent?): Boolean {
        val messageWrittenSuccessfully = NFCUtil.createNFCMessage(
            hashCode.toString(),
            intent,
            isWrite = true
        )
        val text = ifElse(
            messageWrittenSuccessfully as Boolean,
            "Successful Written to Tag",
            "Something When wrong Try Again"
        )
        val view = findViewById<View>(R.id.main_content)
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
        snackbar.addCallback(snackbarCallback())
        snackbar.show()


        if (messageWrittenSuccessfully) {
            // Start counting the usage...
            rentSuccessfully = true
            val timeServiceIntent = Intent(this, TimeCounterService::class.java)
            startService(timeServiceIntent)
        }
        cancelButton?.performClick()
        return messageWrittenSuccessfully
    }


    // STRIPE TRANSACTIONS

    private fun addUserTransactionToFirebaseDB(userId: String, amount: Int) {
        val name = user?.displayName
        val userEmail = user?.email
        val userToSave = com.example.nfc_.helpers.User(
            username = name,
            email = userEmail,
            amount = amount.toDouble(),
            currency = "gbp"
        )
        // if user doesn't exist throw exception ... this shouldn't happen...
        if (user == null) throw Exception("User doesn't exist :(")

        database.child("users").child(userId).child("transactions").child(UUID.randomUUID().toString()).setValue(userToSave)
    }

    private fun isSavedCardToUseValid(): Boolean {
        val card = getSharedPreferences("preferenceName", Context.MODE_PRIVATE).getString("card_to_use", null)
        return card != null && card.isNotEmpty()
    }

    private fun stripe(card: Card, secretKey: String) {
        val paymentIntentParams = createPaymentIntentParams(card, secretKey, null)
        confirmPayment(paymentIntentParams)
    }

    private fun createPaymentIntentParams(
        card: Card,
        clientSecret: String,
        billingDetails: PaymentMethod.BillingDetails?): ConfirmPaymentIntentParams {

        val paymentMethodParamsCard = card.toPaymentMethodParamsCard()
        val paymentMethodCreateParams =
            PaymentMethodCreateParams.create(paymentMethodParamsCard,
                billingDetails)
        return ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
            paymentMethodCreateParams, clientSecret)
    }

    private fun confirmPayment(params: ConfirmPaymentIntentParams) {
        // Optional: customize the Payment Authentication experience
        val uiCustomization = StripeUiCustomization()
        PaymentAuthConfig.init(PaymentAuthConfig.Builder()
            .set3ds2Config(
                PaymentAuthConfig.Stripe3ds2Config.Builder()
                    // set a 5 minute timeout for challenge flow
                    .setTimeout(5)
                    // customize the UI of the challenge flow
                    //.setUiCustomization(uiCustomization)
                    .build())
            .build())

        stripe.confirmPayment(this, params)
    }

}



/**
 * Note that, since extensions do not actually insert members into classes,
 * there's no efficient way for an extension property to have a backing field.
 * This is why initializers are not allowed for extension properties.
 * Their behavior can only be defined by explicitly providing getters/setters.
 */
//val <T> List<T>.bar = 1 // error: initializers are not allowed for extension properties
//Keep reading on ..
//https://kotlinlang.org/docs/reference/extensions.html

