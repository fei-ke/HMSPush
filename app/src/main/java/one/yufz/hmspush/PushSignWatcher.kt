package one.yufz.hmspush

import android.app.AndroidAppHelper
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.io.File


object PushSignWatcher : SharedPreferences.OnSharedPreferenceChangeListener {
    private const val TAG = "PushSignWatcher"

    var lastRegistered: Set<String> = emptySet()
        private set

    private val usePushSign by lazy {
        AndroidAppHelper.currentApplication().createDeviceProtectedStorageContext().let {
            File(it.dataDir, "shared_prefs/PushSign.xml").exists()
        }
    }

    private val pushSignFlow = MutableStateFlow<Set<String>>(emptySet())

    fun watch() {
        XLog.d(TAG, "watch() called, usePushSign = $usePushSign")

        val context = AndroidAppHelper.currentApplication().createDeviceProtectedStorageContext()
        val pushSignPrefName = if (usePushSign) "PushSign" else "pclient_info_v2"
        val pushSignPref = context.getSharedPreferences(pushSignPrefName, Context.MODE_PRIVATE)

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
        return if (usePushSign) {
            lastRegistered
                .map { it.split("/")[0] }
                .toSet()
        } else {
            lastRegistered
        }
    }

    fun observe(): Flow<Set<String>> = pushSignFlow.asStateFlow()
}