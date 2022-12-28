package com.udacity.project4.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.udacity.project4.R

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        // simple method to quickly get radius from settings using just context
        fun getRadiusSettings(context: Context): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val result = sharedPreferences.getInt(context.getString(R.string.radius_settings), 100)
            return result
        }

        // simple method to quickly get signout settings from settings using just context
        fun getSignoutSettings(context: Context): String? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val result = sharedPreferences.getString(context.getString(R.string.signout_settings), context.getString(R.string.keepReminders_settings_option))
            return result
        }
    }

    //set the prefrences that serves as user settings
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
       setPreferencesFromResource(R.xml.settings, rootKey)
    }

}
