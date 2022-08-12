package one.yufz.hmspush

import android.app.AndroidAppHelper
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

object PushHistory {
    private val store by lazy { AndroidAppHelper.currentApplication().getSharedPreferences("push_history", Context.MODE_PRIVATE) }

    private val historyFlow = MutableStateFlow(getAll())

    fun record(packageName: String) {
        store.edit()
            .putLong(packageName, System.currentTimeMillis())
            .apply()

        runBlocking {
            historyFlow.emit(getAll())
        }
    }

    private fun getAll(): Map<String, Long> {
        return store.all.map { it.key to it.value as Long }.toMap()
    }

    fun observe(): Flow<Map<String, Long>> {
        return historyFlow
    }
}