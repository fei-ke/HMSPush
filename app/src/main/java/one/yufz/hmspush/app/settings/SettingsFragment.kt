package one.yufz.hmspush.app.settings

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.view.View
import one.yufz.hmspush.R
import one.yufz.hmspush.common.BridgeWrap
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
        }
        preferenceScreen = preferenceManager.createPreferenceScreen(context)

        val disableSignature = SwitchPreference(context).apply {
            key = "disable_signature"
            setTitle(R.string.disable_signature)
            setSummary(R.string.disable_signature_summary)
            setDefaultValue(BridgeWrap.isDisableSignature(context))
            setOnPreferenceChangeListener { _, newValue ->
                BridgeWrap.setDisableSignature(context, newValue as Boolean)
            }
        }

        preferenceScreen.addPreference(disableSignature)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.WHITE)
    }
}