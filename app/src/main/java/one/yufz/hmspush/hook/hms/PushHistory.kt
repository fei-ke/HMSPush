package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.content.Context
import one.yufz.hmspush.common.BridgeUri

object PushHistory {
    private val store by lazy { AndroidAppHelper.currentApplication().getSharedPreferences("push_history", Context.MODE_PRIVATE) }

    fun record(packageName: String) {
        store.edit()
            .putLong(packageName, System.currentTimeMillis())
            .apply()

        notifyChange()
    }

    fun remove(packageName: String) {
        store.edit()
            .remove(packageName)
            .apply()

        notifyChange()
    }

    fun getAll(): Map<String, Long> {
        return store.all.map { it.key to it.value as Long }.toMap()
    }

    private fun notifyChange() {
        AndroidAppHelper.currentApplication().contentResolver.notifyChange(BridgeUri.PUSH_HISTORY.toUri(), null, false)
    }
}