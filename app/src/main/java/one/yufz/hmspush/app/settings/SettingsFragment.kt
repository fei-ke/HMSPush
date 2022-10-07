package one.yufz.hmspush.app.settings

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import one.yufz.hmspush.R
import one.yufz.hmspush.common.BridgeWrap
import one.yufz.hmspush.common.HMSPUSH_PREF_NAME

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        requireActivity().actionBar?.apply {
            setTitle(R.string.menu_settings)
            setDisplayHomeAsUpEnabled(true)
        }

        preferenceManager.apply {
            sharedPreferencesName = HMSPUSH_PREF_NAME
        }
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())

        val disableSignature = SwitchPreference(requireContext()).apply {
            setIcon(R.drawable.ic_verified)
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