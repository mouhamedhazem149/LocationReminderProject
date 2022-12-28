package com.udacity.project4.locationreminders.remindersdescription

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.AuthActivity
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.geofence.removeGeofenceForReminderIds
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.handleNavigation
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AuthActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    // viewModel retrieved by DI
    val _viewModel: RemindersDescriptionViewModel by inject()

    override val layoutResId: Int
        get() = R.layout.activity_reminder_description

    override val callBackIntent: Intent?
        get() = intent ?: Intent(this, ReminderDescriptionActivity::class.java)

    override fun onAuthenticated() {
//        TODO: Add the implementation of the reminder details

        // trying to get the reminder item that was sent for display using the callback
        // intent, then updaing currentViewModel reminder that is bound to the layout controls
        val activityBinding = binding as ActivityReminderDescriptionBinding
        intent?.let {
            val reminderItem =
                intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

            _viewModel.updateCurrentReminder(reminderItem)

            activityBinding.viewModel = _viewModel
        }

        // monitor any messages to be sent to user
        _viewModel.showToast.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        // monitor if there is any navigation command to deal with it
        // here, the activity just checks that if it was called to go back and there's no stack then
        // it will Exit
        _viewModel.navigationCommand.observe(this, Observer { command ->
            when (command) {
                is NavigationCommand.Back -> {
                    if (!onNavigateUp()) {
                        exitProcess(0)
                    }
                }
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    //inflate options menu to edit or delete
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_description_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                // if user wants to delete an alert dialog is sent to confirm
                AlertDialog.Builder(this)
                    .setMessage(R.string.confirm)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.confirm_delete) { dialogInterface: DialogInterface, i: Int ->
                        _viewModel.deleteCurrentReminder()
                    }
                    .create()
                    .show()
            }
            R.id.edit -> {
                // in case of edit, instead of making new fragment just for this case, the save fragment
                // seems to do all what I need, so i make an intent to start RemindersActivity with an
                // intent that will be used by the activity to navigate to a desired fragment(the savefragment)
                _viewModel.currentReminder.value?.let { reminderItem ->
                    val intent = Intent(this, RemindersActivity::class.java)

                    intent.putExtra(
                        getString(R.string.reminder_navCommand),
                        NavigationCommand.ToFragment(
                            R.id.saveReminderFragment,
                            getString(R.string.current_reminder_key),
                            reminderItem
                        )
                    )
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}