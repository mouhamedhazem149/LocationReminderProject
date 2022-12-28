package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.AsReminderDataItem
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.geofence.addGeofenceForReminder
import com.udacity.project4.settings.SettingsFragment.Companion.getSignoutSettings
import kotlinx.android.synthetic.main.activity_authentication.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val SIGN_IN_REQUEST_CODE = 1001
    }

    private var isInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
//         TODO: If the user was authenticated, send him to RemindersActivity
        UserLiveData().observe(this, Observer {
            checkAuth(it)
        })
    }

    // a function that handle changes in authentication state
    private fun checkAuth(authState: AuthenticationState?) {
        // if Not authenticated then continue launching auth activity
        if (authState != AuthenticationState.AUTHENTICATED) {

            // check if contentView has been initialized with a flag variable
            // to prevent resetting the content view
            if (!isInitialized) {
                setContentView(R.layout.activity_authentication)
                isInitialized = true
            }

//          TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
            login_button.setOnClickListener {
                startSignInFlow()
            }

        } else {

            // check if initialized which mean user was signed out and now signing in,
            // to handle situation related to app settings where user might choose to
            // clear geofences in sign out
            if (isInitialized) {
                checkGeofencesRequests()
            }

            // else if authed then check if intent contained any intent package,
            // and if there is launch it, otherwise launch remindersactivity as default
//          TODO: If the user was authenticated, send him to RemindersActivity
            val callBackIntent =
                intent.getParcelableExtra<Intent>(getString(R.string.authIntent_package_Key))

            if (callBackIntent != null) {
                startActivity(callBackIntent)
            } else {
                startActivity(Intent(this, RemindersActivity::class.java))
            }
        }
    }

    //function to initialize the sign in ui
    private fun startSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        val customLayout = AuthMethodPickerLayout.Builder(R.layout.login_layout)
            .setEmailButtonId(R.id.email_login)
            .setGoogleButtonId(R.id.gmail_login)
            .setTosAndPrivacyPolicyId(R.id.privacypolicy_view)
            .build()

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                //.setLogo(R.drawable.map)
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.AppTheme)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }

    // handle result of signing in
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                startActivity(Intent(this, RemindersActivity::class.java))
            } else {
                Toast.makeText(
                    this,
                    R.string.sign_in_failue,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // check geofences according to user's app settings
    fun checkGeofencesRequests() {

        // in case user choose to delete geofences on signout re add them in login
        when (getSignoutSettings(this)) {
            getString(R.string.keepReminders_settings_option) -> {

                lifecycleScope.launch {
                    val repository: ReminderDataSource by inject()
                    val reminders = repository.getReminders()
                    when (reminders) {
                        is Result.Success<List<ReminderDTO>> -> {
                            LocationServices
                                .getGeofencingClient(applicationContext)
                                .addGeofenceForReminder(*reminders.data.map {
                                    it.AsReminderDataItem()
                                }.toTypedArray())

                            Log.i("Geofence", "Success ReAdd")
                        }
                        else -> {

                        }
                    }
                }
            }
            else -> {

            }
        }
    }
}
