package one.yufz.hmspush.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import one.yufz.hmspush.PushHistory
import one.yufz.hmspush.PushSignWatcher
import one.yufz.hmspush.XLog

class AppListViewModel(val context: Context) {
    companion object {
        private const val TAG = "AppListViewModel"
    }

    private val mainScope = MainScope()

    private val appListFlow = MutableStateFlow<List<String>>(emptyList())

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_CHANGED -> mainScope.launch { loadAppList() }
            }
        }
    }

    fun onCreate() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        context.registerReceiver(packageReceiver, intentFilter)

        mainScope.launch {
            loadAppList()
        }
    }

    private suspend fun loadAppList() {
        val intent = Intent("com.huawei.push.msg.NOTIFY_MSG")
        val list = context.packageManager.queryIntentServices(
            intent,
            PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS or
                    PackageManager.MATCH_DISABLED_COMPONENTS
        )
        XLog.d(TAG, "loadAppList() called, list = ${list.size}")
        list.map { it.serviceInfo.packageName }
            .let { appListFlow.emit(it) }
    }


    fun observeAppList(): Flow<List<AppInfo>> {
        return combine(appListFlow, PushSignWatcher.observe(), PushHistory.observe()) { appList, registered, history ->
            appList.map { AppInfo(it, registered.contains(it), history[it]) }
                .sortedWith(compareBy({ Long.MAX_VALUE - (it.lastPushTime ?: 0L) }, { !it.registered }))
        }
    }

    fun onDestroy() {
        context.unregisterReceiver(packageReceiver)
        mainScope.cancel()
    }
}