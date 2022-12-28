package com.udacity.project4.locationreminders

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.udacity.project4.R
import com.udacity.project4.base.AuthActivity
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.utils.handleNavigation
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AuthActivity() {

    override val layoutResId: Int
        get() = R.layout.activity_reminders

    override val callBackIntent: Intent
        get() = intent ?: Intent(this, RemindersActivity::class.java)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                // navigate to settings screen,
                // menu inflated here to be available across all fragments in the activity
                (nav_host_fragment as NavHostFragment).navController.navigate(R.id.settingsFragment)
            }
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // check if there was intent used to call the activity that was interrupted because
    // of authentication and relaunch it
    override fun onAuthenticated() {
        intent?.let {
            val navCommand =
                intent.getSerializableExtra(getString(R.string.reminder_navCommand)) as NavigationCommand?
            navCommand?.let {
                nav_host_fragment.findNavController().handleNavigation(navCommand)
            }
        }
    }
}
