package com.udacity.project4.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.udacity.project4.R


class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        fun getRadiusSettings(context: Context): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val result = sharedPreferences.getInt(context.getString(R.string.radius_settings), 100)

            return result

        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
       setPreferencesFromResource(R.xml.settings, rootKey)
    }

}
