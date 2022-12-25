package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationState
import com.udacity.project4.authentication.UserLiveData
import kotlinx.android.synthetic.main.activity_reminders.*
import org.jetbrains.annotations.TestOnly

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserLiveData().observe(this, Observer {
            checkAuth(it)
        })
    }

    @TestOnly
    private fun checkAuth(authState : AuthenticationState?) : Boolean {
        // if not authenticated then launch authentication activity
        if (authState != AuthenticationState.AUTHENTICATED) {
            startActivity(
                Intent(
                    this,
                    AuthenticationActivity::class.java
                )
            )

            // used to prevent kind of bleeding of the activity bfr launching the
            // new one and to prevent navigating back to this activity
            finish()
            return false
        }else {
            // else if authed then continue launching reminders activity
            setContentView(R.layout.activity_reminders)
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                (nav_host_fragment as NavHostFragment).navController.navigate(R.id.settingsFragment)
            }
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
