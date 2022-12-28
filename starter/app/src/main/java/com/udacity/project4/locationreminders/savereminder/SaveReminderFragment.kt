package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.base.LocationRequiringFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : LocationRequiringFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // inflating layout
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        // checking navigation argument that may contain reminder to edit
        arguments?.let { bundle ->

            val reminderItem =
                bundle.getSerializable(getString(R.string.current_reminder_key)) as ReminderDataItem?

            reminderItem?.let {
                _viewModel.updateCurrentReminder(it)
            }
        }

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location

            // first check if user has given the necessary permissions
            if (canWorkWithLocation()) {
                // navigate only if requirements are met
                _viewModel.navigationCommand.postValue(
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()))
            } else {
                //else check for permissions and display message explaing need for permissions
                checkLocationPermissionsAndServices()
            }
        }
        // when user try to save the reminder item, first check if he enabled location
        // permission and services , if yes then validate and save, otherwise the
        // onCheckLocationsPermissions will display toast message to explain rationale

        binding.saveReminder.setOnClickListener {
//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            checkLocationPermissionsAndServices()

            if (canWorkWithLocation()) {
                _viewModel.SaveCurrentReminder()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
