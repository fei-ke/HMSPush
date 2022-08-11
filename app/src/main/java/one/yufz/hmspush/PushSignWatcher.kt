package one.yufz.hmspush

import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking


object PushSignWatcher : SharedPreferences.OnSharedPreferenceChangeListener {
    private const val TAG = "PushSignWatcher"

    var lastRegistered: Set<String> = emptySet()
        private set

    private val pushSignFlow = MutableStateFlow(getRegisteredPackageSet())

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

        runBlocking {
            pushSignFlow.emit(getRegisteredPackageSet())
        }
    }

    private fun getAllPackages(perf: SharedPreferences): Set<String> {
        return perf.all.keys.toSet()
    }

    private fun getRegisteredPackageSet(): Set<String> {
        return lastRegistered
            .map { it.split("/")[0] }
            .toSet()
    }

    fun observe(): Flow<Set<String>> = pushSignFlow.asStateFlow()
}