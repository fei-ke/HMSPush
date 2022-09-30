package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences
import one.yufz.hmspush.common.BridgeUri
import one.yufz.hmspush.hook.XLog


object PushSignWatcher : SharedPreferences.OnSharedPreferenceChangeListener {
    private const val TAG = "PushSignWatcher"

    var lastRegistered: Set<String> = emptySet()
        private set

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

        notifyChange()
    }

    private fun notifyChange() {
        AndroidAppHelper.currentApplication().contentResolver.notifyChange(BridgeUri.PUSH_REGISTERED.toUri(), null, false)
    }

    private fun getAllPackages(perf: SharedPreferences): Set<String> {
        return perf.all.keys.toSet()
    }

    private fun getRegisteredPackageSet(): Set<String> {
        return lastRegistered
            .map { it.split("/")[0] }
            .toSet()
    }

    fun unregisterSign(packageName: String) {
        RuntimeKitHook.sendFakePackageRemoveBroadcast(packageName)
    }
}