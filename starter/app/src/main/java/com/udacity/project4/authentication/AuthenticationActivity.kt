package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
import org.jetbrains.annotations.TestOnly

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val  SIGN_IN_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
//         TODO: If the user was authenticated, send him to RemindersActivity
        UserLiveData().observe(this, Observer {
            checkAuth(it)
        })
    }

    @TestOnly
    private fun checkAuth(authState : AuthenticationState?) : Boolean {
        // if Not authenticated then continue launching auth activity
        if (authState != AuthenticationState.AUTHENTICATED) {

            setContentView(R.layout.activity_authentication)

//          TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
            login_button.setOnClickListener {
                startSignInFlow()
            }

            return false
        } else {
            // else if authed then launch the reminder activity
//          TODO: If the user was authenticated, send him to RemindersActivity
            startActivity(Intent(this, RemindersActivity::class.java))

            // used to prevent kind of bleeding of the activity bfr launching the
            // new one and to prevent navigating back to this activity
            finish()

            return true
        }
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                startActivity(Intent(this, RemindersActivity::class.java))
            }
        }

    }
}
