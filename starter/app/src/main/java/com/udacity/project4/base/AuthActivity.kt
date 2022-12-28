package com.udacity.project4.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationState
import com.udacity.project4.authentication.UserLiveData

/**
 * base class for activities that require authentication before they can be viewed
 * provide base functions to see auth state and monitor changes in auth state to deal
 * accordingly
 */
abstract class AuthActivity : AppCompatActivity() {

    // layout that will be set as content
    protected abstract val layoutResId : Int

    // provide a store if activity was called with certain intent to have an approach
    // that the user will likely expected
    protected abstract val callBackIntent : Intent?

    //flag of initializtion of the layout
    private var isInitialized = false

    protected var binding: ViewDataBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // observe the auth state
        UserLiveData().observe(this, Observer {
            checkAuth(it)
        })
    }

    private fun checkAuth(authState : AuthenticationState?) : Boolean {
        // if not authenticated then launch authentication activity
        if (authState != AuthenticationState.AUTHENTICATED) {

            val intent = Intent(
                this,
                AuthenticationActivity::class.java
            )

            // put the calling intent as parcel "callBackIntent",
            // so when authed, it will be called
            intent.putExtra(getString(R.string.authIntent_package_Key), callBackIntent)

            startActivity(
             intent
            )

            // used to prevent kind of bleeding of the activity bfr launching the
            // new one and to prevent navigating back to this activity
            finish()
            return false
        } else {
            // else if authed then continue launching reminders activity
            if (!isInitialized) {

                binding = DataBindingUtil.setContentView(
                    this,
                    layoutResId
                )

                // provide a func to be overriden by derived class if there's certain
                // behaviour that needs to be done on signing in
                onAuthenticated()
                isInitialized = true
            }
            return true
        }
    }

    // provide a func to be overriden by derived class if there's certain
    // behaviour that needs to be done on signing in
    open fun onAuthenticated() {}

}
