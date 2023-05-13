package one.yufz.hmspush.app.home

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import one.yufz.hmspush.app.HmsPushClient
import one.yufz.hmspush.app.fake.ConfigMap
import one.yufz.hmspush.app.fake.FakeDeviceConfig
import one.yufz.hmspush.app.hms.SupportHmsAppList
import one.yufz.hmspush.common.model.PushHistoryModel
import one.yufz.hmspush.common.model.PushSignModel

class AppListViewModel(val context: Application) : AndroidViewModel(context) {
    companion object {
        private const val TAG = "AppListViewModel"
    }

    private val filterKeywords = MutableStateFlow<String>("")

    private val supportedAppList = SupportHmsAppList(context)

    private val registeredListFlow = HmsPushClient.getPushSignFlow()

    private val historyListFlow = HmsPushClient.getPushHistoryFlow()

    val appListFlow: Flow<List<AppInfo>> = combine(supportedAppList.appListFlow, registeredListFlow, historyListFlow, FakeDeviceConfig.configMapFlow, ::mergeSource)
        .combine(filterKeywords, ::filterAppList)

    init {
        viewModelScope.launch {
            FakeDeviceConfig.loadConfig()
            supportedAppList.init()
        }
    }

    private fun filterAppList(list: List<AppInfo>, keywords: String): List<AppInfo> {
        if (keywords.isEmpty()) return list

        return list.filter {
            it.name.contains(keywords, true) || it.packageName.contains(keywords, true)
        }
    }

    private fun mergeSource(appList: List<String>, registered: List<PushSignModel>, history: List<PushHistoryModel>, configMap: ConfigMap): List<AppInfo> {
        val pm = context.packageManager
        val registeredSet = registered.map { it.packageName }
        val historyMap = history.associateBy { it.packageName }
        return appList.map { packageName ->
            AppInfo(
                packageName = packageName,
                name = try {
                    pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    packageName
                },
                registered = registeredSet.contains(packageName),
                lastPushTime = historyMap[packageName]?.pushTime,
                useZygiskFake = configMap.contains(packageName)
            )
        }
            .sortedWith(compareBy({ !it.registered }, { Long.MAX_VALUE - (it.lastPushTime ?: 0L) }))
    }

    fun filter(keywords: String) {
        viewModelScope.launch {
            filterKeywords.emit(keywords)
        }
    }

    fun unregisterPush(packageName: String) {
        HmsPushClient.unregisterPush(packageName)
    }

    fun enableZygiskFake(packageName: String) {
        viewModelScope.launch {
            FakeDeviceConfig.update(packageName, emptyList())
        }
    }

    fun disableZygiskFake(packageName: String) {
        viewModelScope.launch {
            FakeDeviceConfig.deleteConfig(packageName)
        }
    }
}