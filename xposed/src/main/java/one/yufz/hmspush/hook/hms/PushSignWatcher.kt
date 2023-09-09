package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences
import one.yufz.hmspush.common.BridgeUri
import one.yufz.hmspush.common.model.PushSignModel
import one.yufz.hmspush.hook.XLog


object PushSignWatcher : SharedPreferences.OnSharedPreferenceChangeListener {
    private const val TAG = "PushSignWatcher"

    private var lastRegistered: Set<String> = emptySet()

    fun watch() {
        XLog.d(TAG, "watch() called")

        val pushSignPref = AndroidAppHelper.currentApplication().createDeviceProtectedStorageContext()
            .getSharedPreferences("PushSign", Context.MODE_PRIVATE)

        logPushSign(pushSignPref)

        pushSignPref.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
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
        AndroidAppHelper.currentApplication().contentResolver.notifyChange(BridgeUri.PUSH_SIGN.toUri(), null, false)
        HmsPushService.notifyPushSignChanged()
    }

    private fun getAllPackages(perf: SharedPreferences): Set<String> {
        return perf.all.keys.toSet()
    }

    fun getRegisterPackages(): List<PushSignModel> {
        return lastRegistered
            .map { it.split("/") }
            .map { PushSignModel(it[0], it[1].toIntOrNull() ?: 0) }
    }

    fun unregisterSign(packageName: String) {
        RuntimeKitHook.sendFakePackageRemoveBroadcast(packageName)
    }
}