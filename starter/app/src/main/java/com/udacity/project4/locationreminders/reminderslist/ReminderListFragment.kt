package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.LocationRequiringFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.remindersdescription.ReminderDescriptionActivity
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel
// RemidnerlistFragment to display the reminders and inherit the LocationRequiringFragment that
// provides useful methods to check and handle location permissions and settings
class ReminderListFragment : LocationRequiringFragment() {
    //use Koin to retrieve the ViewModel instance

    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }

        // observe if there is any reminder selected, to display its details in ReminderDescriptionActivity
        _viewModel.selectedReminder.observe(viewLifecycleOwner, Observer {
            it?.let {
                val intent = ReminderDescriptionActivity.newIntent(requireContext(), it)
                startActivity(intent)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter { selectedReminder ->
            _viewModel.selectedReminder.value = selectedReminder
        }
//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
//                TODO: add the logout implementation
                _viewModel.onHandleSignOut()
                AuthUI.getInstance().signOut(requireContext())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
    }
}
