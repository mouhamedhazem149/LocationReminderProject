package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.R
import kotlinx.android.synthetic.main.fragment_reminders.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    val context = InstrumentationRegistry.getInstrumentation().getTargetContext()

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    @Test
    fun onReminderListFragmentHome_DisplayedInUi() = runBlockingTest {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        var reminders: RemindersListAdapter

        scenario.onFragment { fragment ->
            var permissionGranted = fragment.isPermissionGranted()

//            // https://stackoverflow.com/questions/36329978/how-to-check-toolbar-title-in-android-instrumental-test
//            val toolbarTitle = context.getString(R.string.app_name)
//            onView(
//                AllOf.allOf(
//                    isAssignableFrom(TextView::class.java), withParent(
//                        isAssignableFrom(
//                            Toolbar::class.java
//                        )
//                    )
//                )
//            ).check(ViewAssertions.matches(withText(toolbarTitle)))

            if (!permissionGranted) {
                onView(withId(com.google.android.material.R.id.snackbar_text))
                    .check(ViewAssertions.matches(withText(context.getString(R.string.permission_denied_explanation))))
            }

            reminders = fragment.reminderssRecyclerView.adapter as RemindersListAdapter

            if (reminders.itemCount == 0) {
                onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
                onView(withId(R.id.noDataTextView)).check(
                    ViewAssertions.matches(
                        withText(
                            context.getString(
                                R.string.no_data
                            )
                        )
                    )
                )
            } else {

                val item = reminders.getItem(0)

                onView(withId(R.id.item_title)).check(ViewAssertions.matches(isDisplayed()))
                onView(withId(R.id.item_title)).check(ViewAssertions.matches(withText(item.title)))

                onView(withId(R.id.item_description)).check(ViewAssertions.matches(isDisplayed()))
                onView(withId(R.id.item_description)).check(ViewAssertions.matches(withText(item.description)))

                onView(withId(R.id.item_location)).check(ViewAssertions.matches(isDisplayed()))
                onView(withId(R.id.item_location)).check(ViewAssertions.matches(withText(item.location)))
            }
        }

    }

    @Test
    fun clickAddReminderButton_navigateToAddReminder() {

        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the add button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the saveReminder screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

}