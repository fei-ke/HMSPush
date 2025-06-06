package one.yufz.hmspush.hook.hms

import android.content.Context
import one.yufz.hmspush.common.model.PushHistoryModel

object PushHistory {
    private val store by lazy { StorageContext.get().getSharedPreferences("push_history", Context.MODE_PRIVATE) }

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

    fun getAll(): List<PushHistoryModel> {
        return store.all.map { PushHistoryModel(it.key, it.value as Long) }
    }

    private fun notifyChange() {
        HmsPushService.notifyPushHistoryChanged()
    }
}
