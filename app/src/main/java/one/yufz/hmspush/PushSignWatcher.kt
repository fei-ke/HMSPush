package one.yufz.hmspush

import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences


class PushSignWatcher : SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val TAG = "PushSignWatcher"
    }

    private var lastRegistered: Set<String> = emptySet()

    fun watch() {
        XLog.d(TAG, "watch() called")

        val pushSignPref = AndroidAppHelper.currentApplication().createDeviceProtectedStorageContext()
            .getSharedPreferences("PushSign", Context.MODE_PRIVATE)

        logPushSign(pushSignPref)

        pushSignPref.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        XLog.d(TAG, "onPushSignChanged() called with: key = $key")
        logPushSign(sharedPreferences)
    }

    private fun logPushSign(pref: SharedPreferences) {
        val newList = getAllPackages(pref)
        val added = newList - lastRegistered

        if (added.isNotEmpty()) {
            XLog.d(TAG, "push registered: $added")
        }

        val removed = lastRegistered - newList

        if (removed.isNotEmpty()) {
            XLog.d(TAG, "push unregister: $removed")
        }

        lastRegistered = newList
    }

    private fun getAllPackages(perf: SharedPreferences): Set<String> {
        return perf.all.keys.toSet()
    }

}