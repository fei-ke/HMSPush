package one.yufz.hmspush.app.hms

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import one.yufz.hmspush.app.util.registerPackageChangeFlow
import one.yufz.hmspush.common.HMS_CORE_PUSH_ACTION_NOTIFY_MSG
import one.yufz.hmspush.common.HMS_CORE_PUSH_ACTION_REGISTRATION

class SupportHmsAppList constructor(private val context: Context) {
    private val _appListFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    val appListFlow: StateFlow<List<String>> = _appListFlow

    suspend fun init() {
        //load once
        _appListFlow.emit(loadSupportedAppList())

        context.registerPackageChangeFlow().collect {
            _appListFlow.emit(loadSupportedAppList())
        }
    }

    suspend fun loadSupportedAppList(): List<String> {
        return withContext(Dispatchers.Default) {
            val queryByReceiver = async(Dispatchers.IO) {
                val intent = Intent(HMS_CORE_PUSH_ACTION_REGISTRATION)
                context.packageManager.queryBroadcastReceivers(
                    intent,
                    PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS
                            or PackageManager.MATCH_DISABLED_COMPONENTS
                ).map { it.activityInfo.packageName }
            }
            val queryByService = async(Dispatchers.IO) {
                val intent = Intent(HMS_CORE_PUSH_ACTION_NOTIFY_MSG)
                context.packageManager.queryIntentServices(
                    intent,
                    PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS
                            or PackageManager.MATCH_DISABLED_COMPONENTS
                ).map { it.serviceInfo.packageName }
            }
            (queryByReceiver.await() + queryByService.await()).distinct()
        }
    }
}