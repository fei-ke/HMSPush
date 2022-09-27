package one.yufz.hmspush.app.settings

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.view.View
import android.widget.Toast
import one.yufz.hmspush.R
import one.yufz.hmspush.common.HMSPUSH_PREF_NAME

class SettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity.actionBar?.apply {
            setTitle(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }

        preferenceManager.apply {
            sharedPreferencesName = HMSPUSH_PREF_NAME
            sharedPreferencesMode = Context.MODE_WORLD_READABLE
        }

        try {
            preferenceManager.getSharedPreferences()
        } catch (e: SecurityException) {
            Toast.makeText(context, R.string.module_not_activated, Toast.LENGTH_SHORT).show()
            fragmentManager.popBackStack()
            return
        }

        preferenceScreen = preferenceManager.createPreferenceScreen(context)

        val disableSignature = SwitchPreference(context).apply {
            key = "disable_signature"
            setTitle(R.string.disable_signature)
            setSummary(R.string.disable_signature_summary)
            setDefaultValue(false)
        }

        preferenceScreen.addPreference(disableSignature)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.WHITE)
    }
}